package com.zpark.wsagent.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话注册表：维护用户与会话、房间成员关系
 * 线程安全，支持多节点时可替换为Redis广播或SharedMap。
 */
@Component
public class ChatSessionRegistry {

    // userId -> sessions
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();
    // roomId -> userIds
    private final Map<String, Set<String>> roomMembers = new ConcurrentHashMap<>();

    /**
     * 从连接URL参数中解析 userId（例如 ws://host/ws/chat?userId=123）
     */
    public Optional<String> resolveUserId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return Optional.empty();
            String query = uri.getQuery();
            if (query == null || query.isEmpty()) return Optional.empty();
            Map<String, String> map = parseQuery(query);
            String uid = map.get("userId");
            if (uid == null || uid.isBlank()) return Optional.empty();
            return Optional.of(uid);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = query.split("&");
        for (String p : pairs) {
            int idx = p.indexOf('=');
            if (idx > 0) {
                String k = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                String v = URLDecoder.decode(p.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    public void addSession(String userId, WebSocketSession session) {
        sessionUser.put(session.getId(), userId);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(WebSocketSession session) {
        String sid = session.getId();
        String userId = sessionUser.remove(sid);
        if (userId != null) {
            Set<WebSocketSession> set = userSessions.get(userId);
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            // 从所有房间移除该用户（如无其他会话仍在）
            cleanupUserFromRoomsIfNoActiveSession(userId);
        }
    }

    private void cleanupUserFromRoomsIfNoActiveSession(String userId) {
        if (userId == null) return;
        if (userSessions.containsKey(userId)) return; // 该用户仍有其它连接
        for (Set<String> members : roomMembers.values()) {
            members.remove(userId);
        }
    }

    public Optional<String> getUserIdBySession(WebSocketSession session) {
        return Optional.ofNullable(sessionUser.get(session.getId()));
    }

    public Set<WebSocketSession> getSessionsByUserId(String userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }

    public void joinRoom(String roomId, String userId) {
        roomMembers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void leaveRoom(String roomId, String userId) {
        Set<String> members = roomMembers.get(roomId);
        if (members != null) {
            members.remove(userId);
            if (members.isEmpty()) {
                roomMembers.remove(roomId);
            }
        }
    }

    public Set<String> getRoomMembers(String roomId) {
        return roomMembers.getOrDefault(roomId, Collections.emptySet());
    }
}
