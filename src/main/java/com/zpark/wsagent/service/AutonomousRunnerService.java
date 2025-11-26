package com.zpark.wsagent.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpark.wsagent.domain.Agent;
import com.zpark.wsagent.domain.Role;
import com.zpark.wsagent.llm.LlmClient;
import com.zpark.wsagent.llm.LlmClient.LlmRequest;
import com.zpark.wsagent.llm.LlmClient.ModelType;
import com.zpark.wsagent.llm.LlmClientFactory;
import com.zpark.wsagent.repository.AgentMapper;
import com.zpark.wsagent.repository.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 自主回合运行服务（Round Runner）
 *
 * - 支持“主动行为机制”：每轮运行中，智能体可自主决定行动（公共发言 / 私信 / 回复 / 保持沉默）
 * - 构造提示(Prompt)：系统记忆 + 动态记忆 + 公共池摘要 + 私信待处理
 * - 期望 LLM 输出 JSON 指令：{"action": "...", "target": "...", "content": "..."}
 * - 解析并执行：调用 ConversationService 的私聊或公共池广播方法
 *
 * 设计约束与约定：
 * - 公共消息池以 roomId="PUBLIC" 表示；公共发言使用 broadcastRoomMessage(roomId="PUBLIC", participantIds=[]) 仅落库不触发即时回应
 * - 私信与回复走 sendPrivateMessage(...)，该方法会生成模型回复；是否需要链式触发由外层控制（多轮）
 * - 公共池摘要与“私信待处理”的数据来源暂为占位方法，当前返回空列表；可后续接入 MessageMapper 的查询（最近N条）
 */
@Service
public class AutonomousRunnerService {

    private final AgentMapper agentMapper;
    private final RoleMapper roleMapper;
    private final MemoryService memoryService;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AutonomousRunnerService(AgentMapper agentMapper,
                                   RoleMapper roleMapper,
                                   MemoryService memoryService,
                                   ConversationService conversationService) {
        this.agentMapper = agentMapper;
        this.roleMapper = roleMapper;
        this.memoryService = memoryService;
        this.conversationService = conversationService;
    }

    /**
     * 运行一轮协作回合
     * @param participantIds 本轮参与的智能体ID列表
     * @param roundId 回合ID（用于消息归档与查询）
     */
    public void runRound(List<Long> participantIds, Long roundId) {
        if (participantIds == null || participantIds.isEmpty()) {
            return;
        }
        for (Long agentId : participantIds) {
            try {
                runForAgent(agentId, roundId);
            } catch (Exception e) {
                // 不中断整体回合，打印错误日志或记录监控（此处简单吞掉）
                // 在生产环境建议使用日志框架记录
            }
        }
    }

    /**
     * 针对单个智能体执行“感知-思考-行动”
     */
    private void runForAgent(Long agentId, Long roundId) throws Exception {
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) return;

        Role role = agent.getRoleId() == null ? null : roleMapper.selectById(agent.getRoleId());
        String systemMemory = role == null ? "" : StringUtils.isBlank(role.getSystemMemory()) ? "" : role.getSystemMemory();

        // 动态记忆窗口（该智能体在公共池的近期交互；可按 roomId 或 session 维度，这里统一用房间窗口）
        String roomKey = memoryService.keyForRoom(agentId, "PUBLIC");
        List<String> dynamicMem = memoryService.getWindow(roomKey);

        // 公共消息池摘要（最近5条，简化为占位）
        List<String> publicSummaries = getPublicPoolSummaries(5);

        // 私信待处理摘要（简化占位）
        List<String> privatePending = getPrivatePendingSummaries(agentId, 5);

        // 构造上下文 Prompt
        String prompt = buildDecisionPrompt(systemMemory, dynamicMem, publicSummaries, privatePending);

        // 选择 LLM 客户端并生成决策 JSON
        ModelType modelType = toModelType(agent.getModelType());
        LlmClient client = LlmClientFactory.get(modelType);

        LlmRequest req = LlmRequest.builder()
                .modelType(modelType)
                .modelName(agent.getModelName())
                .systemMemory("") // 系统记忆将放在“系统记忆”块；避免重复拼接，保留空
                .dynamicMemory(new ArrayList<>()) // 下方 prompt 已包含动态记忆文本
                .userMessage(prompt)
                .agentId(agentId)
                .extraParams(Map.of("roundId", roundId)) // 透传回合ID，便于落库
                .build();

        String decisionJson = client.generate(req);

        // 解析 JSON 指令
        AgentActionDecision decision = parseDecision(decisionJson);

        // 执行动作
        executeDecision(agent, decision, roundId);
    }

    /**
     * 执行解析出的动作
     */
    private void executeDecision(Agent agent, AgentActionDecision decision, Long roundId) {
        if (decision == null || decision.action == Action.SILENCE) {
            // 保持沉默：不做任何事
            return;
        }
        String content = decision.content == null ? "" : decision.content;

        switch (decision.action) {
            case PUBLIC_POST:
                // 在公共池发布（仅落库，不触发即时回应）
                conversationService.broadcastRoomMessage("PUBLIC",
                        agent.getId(),
                        List.of(), // 空列表表示仅写入IN消息，不触发参与者回复
                        content,
                        Map.of("roundId", roundId));
                break;

            case PRIVATE_MESSAGE:
                // 主动私信某个智能体
                Long targetId = safeLong(decision.target);
                if (targetId != null) {
                    conversationService.sendPrivateMessage(agent.getId(), targetId, sessionIdFor(agent.getId(), targetId), content, Map.of("roundId", roundId));
                }
                break;

            case REPLY:
                // 简化实现：回复公共池（也可根据 target 决定是私信还是公共回复）
                if (decision.target != null && decision.target.startsWith("agent_")) {
                    Long replyTarget = safeLong(decision.target);
                    if (replyTarget != null) {
                        conversationService.sendPrivateMessage(agent.getId(), replyTarget, sessionIdFor(agent.getId(), replyTarget), content, Map.of("roundId", roundId));
                        break;
                    }
                }
                conversationService.broadcastRoomMessage("PUBLIC",
                        agent.getId(),
                        List.of(),
                        content,
                        Map.of("roundId", roundId));
                break;
        }
    }

    /**
     * 构造决策提示模板（Prompt Engineering）
     */
    private String buildDecisionPrompt(String systemMemory,
                                       List<String> dynamicMem,
                                       List<String> publicSummaries,
                                       List<String> privatePending) {
        StringBuilder sb = new StringBuilder();
        sb.append("[系统记忆]\n")
          .append(systemMemory == null ? "" : systemMemory).append("\n\n");

        sb.append("[当前任务]\n")
          .append("请根据以下信息决定是否采取行动。你可以：1) 在公共池发言，2) 私信某专家，3) 回复特定消息，4) 保持沉默。\n\n");

        sb.append("[最近对话历史]\n");
        if (dynamicMem != null && !dynamicMem.isEmpty()) {
            for (String m : dynamicMem) {
                if (!StringUtils.isBlank(m)) {
                    sb.append("- ").append(m.trim()).append("\n");
                }
            }
        } else {
            sb.append("- (空)\n");
        }
        sb.append("\n");

        sb.append("[公共消息池最新摘要]\n");
        if (publicSummaries != null && !publicSummaries.isEmpty()) {
            for (String s : publicSummaries) {
                if (!StringUtils.isBlank(s)) {
                    sb.append("- ").append(s.trim()).append("\n");
                }
            }
        } else {
            sb.append("- (空)\n");
        }
        sb.append("\n");

        sb.append("[私信待处理]\n");
        if (privatePending != null && !privatePending.isEmpty()) {
            for (String s : privatePending) {
                if (!StringUtils.isBlank(s)) {
                    sb.append("- ").append(s.trim()).append("\n");
                }
            }
        } else {
            sb.append("- (空)\n");
        }
        sb.append("\n");

        sb.append("请以JSON(JavaScript Object Notation)格式输出你的行动：\n")
          .append("{\"action\": \"public_post\" | \"private_message\" | \"reply\" | \"silence\", \"target\": \"agent_03 或 智能体ID(可选)\", \"content\": \"...\"}\n");

        return sb.toString();
    }

    /**
     * 将字符串映射到模型类型枚举（大小写不敏感）
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
     * 解析 LLM 输出的 JSON 指令
     */
    private AgentActionDecision parseDecision(String json) {
        if (json == null || json.isBlank()) {
            return new AgentActionDecision(Action.SILENCE, null, null);
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            String actionStr = text(root, "action");
            String target = text(root, "target");
            String content = text(root, "content");
            Action action = Action.fromString(actionStr);
            return new AgentActionDecision(action, target, content);
        } catch (Exception e) {
            // 解析失败时保持沉默，避免异常中断
            return new AgentActionDecision(Action.SILENCE, null, null);
        }
    }

    private String text(JsonNode root, String field) {
        if (root == null) return null;
        JsonNode n = root.get(field);
        return (n == null || n.isNull()) ? null : n.asText();
    }

    private Long safeLong(String target) {
        if (StringUtils.isBlank(target)) return null;
        // 支持 "agent_03" 或纯数字ID
        try {
            if (target.startsWith("agent_")) {
                return Long.parseLong(target.substring("agent_".length()));
            }
            return Long.parseLong(target);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sessionIdFor(Long a, Long b) {
        // 简单生成私聊会话ID（可替换为更复杂的会话管理）
        long x = (a == null ? 0L : a);
        long y = (b == null ? 0L : b);
        long lo = Math.min(x, y);
        long hi = Math.max(x, y);
        return "s-" + lo + "-" + hi;
    }

    /**
     * 占位：公共池最近N条摘要
     * TODO：可改为使用 MessageMapper 查询 roomId="PUBLIC" 的最近消息并摘要
     */
    private List<String> getPublicPoolSummaries(int n) {
        return new ArrayList<>();
    }

    /**
     * 占位：该智能体的私信待处理摘要
     * TODO：可改为使用 MessageMapper 查询 receiverAgentId=agentId 且 direction="IN" 的最近消息
     */
    private List<String> getPrivatePendingSummaries(Long agentId, int n) {
        return new ArrayList<>();
    }

    /**
     * 指令动作枚举
     */
    enum Action {
        PUBLIC_POST("public_post"),
        PRIVATE_MESSAGE("private_message"),
        REPLY("reply"),
        SILENCE("silence");

        final String v;
        Action(String v) { this.v = v; }

        static Action fromString(String s) {
            if (s == null) return SILENCE;
            String t = s.trim().toLowerCase(Locale.ROOT);
            for (Action a : values()) {
                if (a.v.equals(t)) return a;
            }
            return SILENCE;
        }
    }

    /**
     * 指令数据结构
     */
    static class AgentActionDecision {
        final Action action;
        final String target;
        final String content;

        AgentActionDecision(Action action, String target, String content) {
            this.action = action;
            this.target = target;
            this.content = content;
        }
    }
}
