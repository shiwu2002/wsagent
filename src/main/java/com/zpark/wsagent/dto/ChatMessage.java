package com.zpark.wsagent.dto;

import lombok.Data;

/**
 * 客户端与服务端之间传输的消息模型
 * - type: 消息类型，见 MessageType
 * - fromUserId: 发送方用户ID（由服务端在收到消息时补齐）
 * - toUserId: 私聊目标用户ID（仅 PRIVATE_MSG 使用）
 * - roomId: 群聊房间ID（JOIN_GROUP/LEAVE_GROUP/GROUP_MSG 使用）
 * - content: 文本内容
 * - clientMsgId: 客户端自定义的消息ID，用于回执关联（可选）
 */
@Data
public class ChatMessage {
    private MessageType type;
    private String fromUserId;
    private String toUserId;
    private String roomId;
    private String content;
    private String clientMsgId;
}
