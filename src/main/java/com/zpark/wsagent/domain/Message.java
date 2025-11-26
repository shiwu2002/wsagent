package com.zpark.wsagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 消息实体（持久化所有对话数据）
 *
 * - 支持点对点私聊与群组讨论（通过 sessionId / roomId 进行上下文隔离）
 * - 记录发送者、接收对象、内容、模型、角色ID、时间戳等
 * - 与 Redis 中的动态记忆窗口搭配使用：此处保存完整历史，Redis仅缓存最近N轮
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("messages")
public class Message {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者智能体ID */
    private Long senderAgentId;

    /** 接收者智能体ID（私聊时必填；群组讨论时可为空） */
    private Long receiverAgentId;

    /** 私聊会话ID（点对点通道） */
    private String sessionId;

    /** 讨论室ID（群组通道） */
    private String roomId;

    /** 消息内容（纯文本，必要时可扩展为JSON结构） */
    private String content;

    /** 使用的大模型类型（与 LlmClient.ModelType 对应的字符串） */
    private String modelType;

    /** 使用的具体模型名称（如 "qwen-turbo"、"gpt-4o-mini" 等） */
    private String modelName;

    /** 关联角色ID（用于追溯该消息生成时绑定的角色与系统记忆） */
    private Long roleId;

    /** 消息方向：IN(来自用户/外部) 或 OUT(模型生成)；可用于区分人类输入与AI回复 */
    private String direction;

    /** 创建时间（epoch毫秒） */
    private Long createdAt;

    /** 所属运行回合ID（用于“协作回合”归档与追踪） */
    private Long roundId;

    /** 是否为自主行为（true=由智能体主动行动；false=响应外部或他人消息） */
    private Boolean isAutonomous;

    /** 备用扩展字段，存储元数据（如温度、top_p、工具调用信息等），建议使用JSON字符串 */
    private String metadata;
}
