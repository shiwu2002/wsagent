package com.zpark.wsagent.emtity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息持久化实体，对应表 chat_messages
 */
@Data
@TableName("chat_messages")
public class ChatMessageEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息类型：GROUP_MSG/PRIVATE_MSG */
    private String type;

    /** 发送方用户ID */
    private String fromUserId;

    /** 私聊目标用户ID（群聊为空） */
    private String toUserId;

    /** 群聊房间ID（私聊为空） */
    private String roomId;

    /** 消息内容 */
    private String content;

    /** 创建时间（数据库默认CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;
}
