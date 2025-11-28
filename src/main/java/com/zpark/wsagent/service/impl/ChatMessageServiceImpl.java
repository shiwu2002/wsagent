package com.zpark.wsagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.ChatMessageEntity;
import com.zpark.wsagent.mapper.ChatMessageMapper;
import com.zpark.wsagent.service.ChatMessageService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageServiceImpl(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public ChatMessageEntity save(String type, String fromUserId, String toUserId, String roomId, String content) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setType(type);
        entity.setFromUserId(fromUserId);
        entity.setToUserId(toUserId);
        entity.setRoomId(roomId);
        entity.setContent(content);
        // createdAt 由数据库默认值生成
        chatMessageMapper.insert(entity);
        return entity;
    }

    @Override
    public IPage<ChatMessageEntity> pageGroupHistory(Page<ChatMessageEntity> page,
                                             String roomId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime) {
        QueryWrapper<ChatMessageEntity> qw = new QueryWrapper<>();
        qw.eq("room_id", roomId)
          .isNull("to_user_id"); // 群聊无 to_user_id
        if (startTime != null) {
            qw.ge("created_at", startTime);
        }
        if (endTime != null) {
            qw.le("created_at", endTime);
        }
        qw.orderByAsc("created_at").orderByAsc("id");
        return chatMessageMapper.selectPage(page, qw);
    }

    @Override
    public IPage<ChatMessageEntity> pagePrivateHistory(Page<ChatMessageEntity> page,
                                               String userA,
                                               String userB,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime) {
        QueryWrapper<ChatMessageEntity> qw = new QueryWrapper<>();
        qw.isNull("room_id") // 私聊无 room_id
          .and(w -> w
                .nested(n -> n.eq("from_user_id", userA).eq("to_user_id", userB))
                .or(n -> n.eq("from_user_id", userB).eq("to_user_id", userA))
          );
        if (startTime != null) {
            qw.ge("created_at", startTime);
        }
        if (endTime != null) {
            qw.le("created_at", endTime);
        }
        qw.orderByAsc("created_at").orderByAsc("id");
        return chatMessageMapper.selectPage(page, qw);
    }
}
