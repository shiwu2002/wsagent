package com.zpark.wsagent.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zpark.wsagent.domain.Agent;
import com.zpark.wsagent.domain.Message;
import com.zpark.wsagent.domain.Role;
import com.zpark.wsagent.llm.LlmClient;
import com.zpark.wsagent.llm.LlmClient.LlmRequest;
import com.zpark.wsagent.llm.LlmClient.ModelType;
import com.zpark.wsagent.llm.LlmClientFactory;
import com.zpark.wsagent.repository.AgentMapper;
import com.zpark.wsagent.repository.MessageMapper;
import com.zpark.wsagent.repository.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 会话协作核心服务
 *
 * - 点对点私聊：两智能体间消息往来，按 sessionId 隔离上下文
 * - 群组讨论：讨论室内广播，智能体各自基于记忆独立响应
 * - 动态记忆：通过 MemoryService 管理最近 N 轮窗口（Redis）
 * - 数据持久化：所有消息写入 MySQL（messages）
 */
@Service
public class ConversationService {

    private final AgentMapper agentMapper;
    private final RoleMapper roleMapper;
    private final MessageMapper messageMapper;
    private final MemoryService memoryService;

    public ConversationService(AgentMapper agentMapper,
                               RoleMapper roleMapper,
                               MessageMapper messageMapper,
                               MemoryService memoryService) {
        this.agentMapper = agentMapper;
        this.roleMapper = roleMapper;
        this.messageMapper = messageMapper;
        this.memoryService = memoryService;
    }

    /**
     * 将字符串模型类型映射到枚举（大小写不敏感）
     */
    private ModelType toModelType(String type) {
        if (type == null) return null;
        try {
            return ModelType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 发送一条私聊消息，并获得目标智能体的回复
     *
     * 过程：
     * 1) 持久化来信（IN）
     * 2) 读取接收者的系统记忆（角色）与动态记忆窗口（Redis）
     * 3) 选择模型客户端，构造 LlmRequest，生成回复
     * 4) 持久化回复（OUT）
     * 5) 将来信与回复摘要写入接收者窗口（可选也写入发送者窗口）
     *
     * @param senderAgentId 发送者智能体ID
     * @param receiverAgentId 接收者智能体ID
     * @param sessionId 私聊会话ID
     * @param content 文本内容
     * @param extraParams 模型额外参数（温度、最大token等）
     * @return 模型生成的回复文本
     */
    @Transactional
    public String sendPrivateMessage(Long senderAgentId,
                                     Long receiverAgentId,
                                     String sessionId,
                                     String content,
                                     Map<String, Object> extraParams) {

        long now = System.currentTimeMillis();
        Long roundId = (extraParams != null && extraParams.get("roundId") instanceof Number)
                ? ((Number) extraParams.get("roundId")).longValue()
                : null;

        // 1) 持久化来信（IN）
        Message inMsg = new Message()
                .setSenderAgentId(senderAgentId)
                .setReceiverAgentId(receiverAgentId)
                .setSessionId(sessionId)
                .setContent(content)
                .setDirection("IN")
                .setCreatedAt(now)
                .setRoundId(roundId)
                .setIsAutonomous(Boolean.FALSE);
        messageMapper.insert(inMsg);

        // 2) 读取接收者配置：模型、角色系统记忆；获取动态记忆窗口
        Agent receiver = agentMapper.selectById(receiverAgentId);
        if (receiver == null) {
            throw new IllegalArgumentException("接收者智能体不存在: " + receiverAgentId);
        }
        Role role = receiver.getRoleId() == null ? null : roleMapper.selectById(receiver.getRoleId());
        String systemMemory = role == null ? "" : StringUtils.isBlank(role.getSystemMemory()) ? "" : role.getSystemMemory();

        String windowKey = memoryService.keyForSession(receiverAgentId, sessionId);
        List<String> dynamicMem = memoryService.getWindow(windowKey);

        // 3) 选择模型客户端，构造请求
        ModelType modelType = toModelType(receiver.getModelType());
        LlmClient client = LlmClientFactory.get(modelType);

        LlmRequest req = LlmRequest.builder()
                .modelType(modelType)
                .modelName(receiver.getModelName())
                .systemMemory(systemMemory)
                .dynamicMemory(dynamicMem)
                .userMessage(content)
                .sessionId(sessionId)
                .agentId(receiverAgentId)
                .extraParams(extraParams)
                .build();

        String reply = client.generate(req);

        // 4) 持久化回复（OUT）
        Message outMsg = new Message()
                .setSenderAgentId(receiverAgentId)
                .setReceiverAgentId(senderAgentId)
                .setSessionId(sessionId)
                .setContent(reply)
                .setModelType(receiver.getModelType())
                .setModelName(receiver.getModelName())
                .setRoleId(receiver.getRoleId())
                .setDirection("OUT")
                .setCreatedAt(System.currentTimeMillis())
                .setRoundId(roundId)
                .setIsAutonomous(Boolean.TRUE);
        messageMapper.insert(outMsg);

        // 5) 更新接收者的动态记忆窗口
        memoryService.append(windowKey, "对方: " + content);
        memoryService.append(windowKey, "我方: " + reply);

        return reply;
    }

    /**
     * 在讨论室内广播一条消息，房间内每个智能体各自回复
     *
     * 过程：
     * 1) 持久化来信（IN），senderAgentId 可为人类代理或某个系统代理
     * 2) 对房间内每个Agent：
     *    - 读取其系统记忆 + 动态窗口
     *    - 调用对应模型生成回复
     *    - 持久化该Agent的回复（OUT）
     *    - 将来信与其回复写入该Agent的房间窗口
     *
     * @param roomId 讨论室ID
     * @param senderAgentId 发送者智能体ID（可选）
     * @param participantIds 房间参与的智能体ID列表
     * @param content 广播内容
     * @param extraParams 模型额外参数
     * @return 每个参与者的回复拼接（简单返回），也可改为结构化返回
     */
    @Transactional
    public String broadcastRoomMessage(String roomId,
                                       Long senderAgentId,
                                       List<Long> participantIds,
                                       String content,
                                       Map<String, Object> extraParams) {
        long now = System.currentTimeMillis();
        Long roundId = (extraParams != null && extraParams.get("roundId") instanceof Number)
                ? ((Number) extraParams.get("roundId")).longValue()
                : null;

        // 持久化广播消息（IN）
        Message inMsg = new Message()
                .setSenderAgentId(senderAgentId)
                .setRoomId(roomId)
                .setContent(content)
                .setDirection("IN")
                .setCreatedAt(now)
                .setRoundId(roundId)
                .setIsAutonomous(Boolean.FALSE);
        messageMapper.insert(inMsg);

        StringBuilder sb = new StringBuilder();

        for (Long agentId : participantIds) {
            Agent agent = agentMapper.selectById(agentId);
            if (agent == null) {
                sb.append("Agent ").append(agentId).append(" 不存在，跳过。\n");
                continue;
            }
            Role role = agent.getRoleId() == null ? null : roleMapper.selectById(agent.getRoleId());
            String systemMemory = role == null ? "" : StringUtils.isBlank(role.getSystemMemory()) ? "" : role.getSystemMemory();

            String windowKey = memoryService.keyForRoom(agentId, roomId);
            List<String> dynamicMem = memoryService.getWindow(windowKey);

            ModelType modelType = toModelType(agent.getModelType());
            LlmClient client = LlmClientFactory.get(modelType);

            LlmRequest req = LlmRequest.builder()
                    .modelType(modelType)
                    .modelName(agent.getModelName())
                    .systemMemory(systemMemory)
                    .dynamicMemory(dynamicMem)
                    .userMessage(content)
                    .roomId(roomId)
                    .agentId(agentId)
                    .extraParams(extraParams)
                    .build();

            String reply = client.generate(req);

            // 持久化该Agent的回复（OUT）
            Message outMsg = new Message()
                    .setSenderAgentId(agentId)
                    .setRoomId(roomId)
                    .setContent(reply)
                    .setModelType(agent.getModelType())
                    .setModelName(agent.getModelName())
                    .setRoleId(agent.getRoleId())
                    .setDirection("OUT")
                    .setCreatedAt(System.currentTimeMillis())
                    .setRoundId(roundId)
                    .setIsAutonomous(Boolean.TRUE);
            messageMapper.insert(outMsg);

            // 更新该Agent的房间窗口
            memoryService.append(windowKey, "广播: " + content);
            memoryService.append(windowKey, "回复: " + reply);

            sb.append("Agent ").append(agentId).append(" 回复：").append(reply).append("\n");
        }

        return sb.toString();
    }
}
