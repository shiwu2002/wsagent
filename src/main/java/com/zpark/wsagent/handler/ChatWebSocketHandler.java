package com.zpark.wsagent.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zpark.wsagent.dto.ChatMessage;
import com.zpark.wsagent.dto.MessageType;
import com.zpark.wsagent.service.ChatMessageService;
import com.zpark.wsagent.websocket.ChatSessionRegistry;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * WebSocket文本处理器：
 * - 连接建立时基于URL参数获取userId并注册会话
 * - 处理消息类型：JOIN_GROUP / LEAVE_GROUP / GROUP_MSG / PRIVATE_MSG
 * - 将消息路由到群成员或目标用户（支持同一用户多连接）
 * - 发送ACK和ERROR回执
 * - 将群聊与私聊消息持久化到数据库
 *
 * 客户端示例连接：
 *   const ws = new WebSocket("ws://localhost:8080/ws/chat?userId=alice");
 * 发送JSON（使用JSON(JavaScript Object Notation)格式）：
 *   { "type":"JOIN_GROUP","roomId":"room-1" }
 *   { "type":"GROUP_MSG","roomId":"room-1","content":"大家好" }
 *   { "type":"PRIVATE_MSG","toUserId":"bob","content":"你好，Bob" }
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatSessionRegistry registry;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(ChatSessionRegistry registry,
                                ChatMessageService chatMessageService) {
        this.registry = registry;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Optional<String> userOpt = registry.resolveUserId(session);
        if (userOpt.isEmpty()) {
            sendError(session, null, "缺少userId参数，连接被拒绝");
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String userId = userOpt.get();
        registry.addSession(userId, session);
        // 回执连接成功
        sendAck(session, null, "连接成功，userId=" + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage msg;
        try {
            msg = mapper.readValue(message.getPayload(), ChatMessage.class);
        } catch (Exception e) {
            sendError(session, null, "消息格式错误：" + e.getMessage());
            return;
        }

        Optional<String> fromOpt = registry.getUserIdBySession(session);
        if (fromOpt.isEmpty()) {
            sendError(session, msg != null ? msg.getClientMsgId() : null, "未识别的用户会话");
            return;
        }
        String fromUserId = fromOpt.get();
        if (msg == null || msg.getType() == null) {
            sendError(session, null, "缺少消息类型");
            return;
        }
        // 补齐发送方
        msg.setFromUserId(fromUserId);

        try {
            switch (msg.getType()) {
                case JOIN_GROUP:
                    handleJoinGroup(session, msg);
                    break;
                case LEAVE_GROUP:
                    handleLeaveGroup(session, msg);
                    break;
                case GROUP_MSG:
                    handleGroupMsg(session, msg);
                    break;
                case PRIVATE_MSG:
                    handlePrivateMsg(session, msg);
                    break;
                default:
                    sendError(session, msg.getClientMsgId(), "不支持的消息类型: " + msg.getType());
            }
        } catch (Exception e) {
            sendError(session, msg.getClientMsgId(), "处理失败：" + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        registry.removeSession(session);
    }

    private void handleJoinGroup(WebSocketSession session, ChatMessage msg) throws IOException {
        if (isBlank(msg.getRoomId())) {
            sendError(session, msg.getClientMsgId(), "JOIN_GROUP缺少roomId");
            return;
        }
        registry.joinRoom(msg.getRoomId(), msg.getFromUserId());
        sendAck(session, msg.getClientMsgId(), "已加入房间：" + msg.getRoomId());
    }

    private void handleLeaveGroup(WebSocketSession session, ChatMessage msg) throws IOException {
        if (isBlank(msg.getRoomId())) {
            sendError(session, msg.getClientMsgId(), "LEAVE_GROUP缺少roomId");
            return;
        }
        registry.leaveRoom(msg.getRoomId(), msg.getFromUserId());
        sendAck(session, msg.getClientMsgId(), "已离开房间：" + msg.getRoomId());
    }

    private void handleGroupMsg(WebSocketSession session, ChatMessage msg) throws IOException {
        if (isBlank(msg.getRoomId())) {
            sendError(session, msg.getClientMsgId(), "GROUP_MSG缺少roomId");
            return;
        }
        if (isBlank(msg.getContent())) {
            sendError(session, msg.getClientMsgId(), "GROUP_MSG缺少content");
            return;
        }
        // 持久化
        chatMessageService.save(MessageType.GROUP_MSG.name(),
                msg.getFromUserId(), null, msg.getRoomId(), msg.getContent());

        // 广播到房间内所有成员的所有连接
        Set<String> members = registry.getRoomMembers(msg.getRoomId());
        for (String uid : members) {
            sendToUser(uid, msg);
        }
        // 向发送者回执
        sendAck(session, msg.getClientMsgId(), "群消息已投递并持久化到房间：" + msg.getRoomId());
    }

    private void handlePrivateMsg(WebSocketSession session, ChatMessage msg) throws IOException {
        if (isBlank(msg.getToUserId())) {
            sendError(session, msg.getClientMsgId(), "PRIVATE_MSG缺少toUserId");
            return;
        }
        if (isBlank(msg.getContent())) {
            sendError(session, msg.getClientMsgId(), "PRIVATE_MSG缺少content");
            return;
        }
        // 持久化
        chatMessageService.save(MessageType.PRIVATE_MSG.name(),
                msg.getFromUserId(), msg.getToUserId(), null, msg.getContent());

        // 发送给目标用户的所有连接
        sendToUser(msg.getToUserId(), msg);
        // 向发送者回执
        sendAck(session, msg.getClientMsgId(), "私聊消息已发送并持久化给：" + msg.getToUserId());
    }

    private void sendToUser(String userId, ChatMessage msg) throws IOException {
        Set<WebSocketSession> sessions = registry.getSessionsByUserId(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload = mapper.writeValueAsString(msg);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    private void sendAck(WebSocketSession session, String clientMsgId, String content) throws IOException {
        ChatMessage ack = new ChatMessage();
        ack.setType(MessageType.ACK);
        ack.setClientMsgId(clientMsgId);
        ack.setContent(content);
        String payload = mapper.writeValueAsString(ack);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(payload));
        }
    }

    private void sendError(WebSocketSession session, String clientMsgId, String content) throws IOException {
        ChatMessage err = new ChatMessage();
        err.setType(MessageType.ERROR);
        err.setClientMsgId(clientMsgId);
        err.setContent(content);
        String payload = mapper.writeValueAsString(err);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(payload));
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
