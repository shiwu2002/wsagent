package com.zpark.wsagent.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.ChatMessageEntity;

import java.time.LocalDateTime;

public interface ChatMessageService {

    /**
     * 持久化一条消息
     */
    ChatMessageEntity save(String type, String fromUserId, String toUserId, String roomId, String content);

    /**
     * 分页查询群聊历史
     * - 按时间范围过滤
     * - 按创建时间升序返回
     */
    IPage<ChatMessageEntity> pageGroupHistory(Page<ChatMessageEntity> page,
                                  String roomId,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime);

    /**
     * 分页查询私聊历史（双向）
     * - userA 与 userB 之间的消息（A->B 或 B->A）
     * - 可按时间范围过滤
     * - 按创建时间升序返回
     */
    IPage<ChatMessageEntity> pagePrivateHistory(Page<ChatMessageEntity> page,
                                    String userA,
                                    String userB,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime);
}
