package com.zpark.wsagent.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zpark.wsagent.domain.Message;
import com.zpark.wsagent.repository.MessageMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息查询控制器（前后端分离，供 React 前端调用）
 *
 * - 提供公共消息池、私信收件箱、按房间/会话查询等只读接口
 * - React 前端可通过 fetch/axios 调用这些 REST API 拉取展示数据
 */
@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private final MessageMapper messageMapper;

    public MessagesController(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 公共消息池最新消息（roomId="PUBLIC"）
     * GET /api/messages/public?limit=5
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicFeed(@RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        int n = Math.max(1, Math.min(limit, 100));
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("room_id", "PUBLIC")
                .orderByDesc("created_at")
                .last("LIMIT " + n);
        List<Message> list = messageMapper.selectList(qw);
        return ResponseEntity.ok(Map.of("items", list));
    }

    /**
     * 私信收件箱（按接收者智能体ID）
     * GET /api/messages/private/{agentId}?limit=5
     * 仅返回 direction="IN" 的消息（他人发给该智能体）
     */
    @GetMapping("/private/{agentId}")
    public ResponseEntity<Map<String, Object>> getPrivateInbox(@PathVariable("agentId") Long agentId,
                                                               @RequestParam(name = "limit", defaultValue = "10") Integer limit) {
        int n = Math.max(1, Math.min(limit, 100));
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("receiver_agent_id", agentId)
                .eq("direction", "IN")
                .orderByDesc("created_at")
                .last("LIMIT " + n);
        List<Message> list = messageMapper.selectList(qw);
        return ResponseEntity.ok(Map.of("items", list));
    }

    /**
     * 按讨论室(roomId)查询最近消息
     * GET /api/messages/room/{roomId}?limit=20
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomFeed(@PathVariable("roomId") String roomId,
                                                           @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        int n = Math.max(1, Math.min(limit, 200));
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("room_id", roomId)
                .orderByDesc("created_at")
                .last("LIMIT " + n);
        List<Message> list = messageMapper.selectList(qw);
        return ResponseEntity.ok(Map.of("items", list));
    }

    /**
     * 按会话(sessionId)查询最近消息（点对点）
     * GET /api/messages/session/{sessionId}?limit=50
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionThread(@PathVariable("sessionId") String sessionId,
                                                                @RequestParam(name = "limit", defaultValue = "50") Integer limit) {
        int n = Math.max(1, Math.min(limit, 500));
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("session_id", sessionId)
                .orderByDesc("created_at")
                .last("LIMIT " + n);
        List<Message> list = messageMapper.selectList(qw);
        return ResponseEntity.ok(Map.of("items", list));
    }

    /**
     * 按回合ID(roundId)过滤公共池（可用于前端展示某一运行回合的轨迹）
     * GET /api/messages/public/by-round?roundId=1001&limit=20
     */
    @GetMapping("/public/by-round")
    public ResponseEntity<Map<String, Object>> getPublicByRound(@RequestParam("roundId") Long roundId,
                                                                @RequestParam(name = "limit", defaultValue = "20") Integer limit) {
        int n = Math.max(1, Math.min(limit, 200));
        QueryWrapper<Message> qw = new QueryWrapper<Message>()
                .eq("room_id", "PUBLIC")
                .eq("round_id", roundId)
                .orderByDesc("created_at")
                .last("LIMIT " + n);
        List<Message> list = messageMapper.selectList(qw);
        return ResponseEntity.ok(Map.of("items", list));
    }
}
