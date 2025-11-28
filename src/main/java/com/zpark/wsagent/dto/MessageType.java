package com.zpark.wsagent.dto;

/**
 * 消息类型定义
 * - JOIN_GROUP: 加入群组（房间）
 * - LEAVE_GROUP: 离开群组
 * - GROUP_MSG: 群聊消息
 * - PRIVATE_MSG: 私聊消息
 * - ACK: 服务端确认/回执
 * - ERROR: 错误消息
 */
public enum MessageType {
    JOIN_GROUP,
    LEAVE_GROUP,
    GROUP_MSG,
    PRIVATE_MSG,
    ACK,
    ERROR
}
