package com.zpark.wsagent.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zpark.wsagent.emtity.ChatMessageEntity;
import com.zpark.wsagent.service.ChatMessageService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 聊天记录查询控制器
 * - 群聊历史：GET /api/chat/group/{roomId}/messages
 * - 私聊历史：GET /api/chat/private/messages
 *
 * 分页参数：
 * - page: 第几页（从1开始，默认1）
 * - size: 每页大小（默认20）
 * 过滤参数（可选）：
 * - startTime: 开始时间（ISO，例如 2025-01-01T00:00:00）
 * - endTime: 结束时间（ISO）
 *
 * 返回：
 * - IPage<ChatMessageEntity>，包括 total/current/size/records 等
 */
@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /**
     * 分页查询群聊历史
     * 示例：
     * GET /api/chat/group/room-1/messages?page=1&size=20&startTime=2025-01-01T00:00:00&endTime=2025-12-31T23:59:59
     */
    @GetMapping("/group/{roomId}/messages")
    public IPage<ChatMessageEntity> pageGroupHistory(
            @PathVariable("roomId") String roomId,
            @RequestParam(value = "page", defaultValue = "1") long pageNum,
            @RequestParam(value = "size", defaultValue = "20") long pageSize,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        Page<ChatMessageEntity> page = new Page<>(pageNum, pageSize);
        return chatMessageService.pageGroupHistory(page, roomId, startTime, endTime);
    }

    /**
     * 分页查询私聊历史（双向）
     * 示例：
     * GET /api/chat/private/messages?userA=alice&userB=bob&page=1&size=20
     */
    @GetMapping("/private/messages")
    public IPage<ChatMessageEntity> pagePrivateHistory(
            @RequestParam("userA") String userA,
            @RequestParam("userB") String userB,
            @RequestParam(value = "page", defaultValue = "1") long pageNum,
            @RequestParam(value = "size", defaultValue = "20") long pageSize,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        Page<ChatMessageEntity> page = new Page<>(pageNum, pageSize);
        return chatMessageService.pagePrivateHistory(page, userA, userB, startTime, endTime);
    }
}
