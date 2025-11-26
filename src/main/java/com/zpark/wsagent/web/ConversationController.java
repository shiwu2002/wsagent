package com.zpark.wsagent.web;

import com.zpark.wsagent.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话协作控制器
 *
 * - 暴露私聊与群组讨论的HTTP接口
 * - 输入/输出采用简单JSON(键值对)格式，便于前端或Postman调试
 */
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 私聊：发送消息并获取接收者的回复
     *
     * 请求JSON：
     * {
     *   "senderAgentId": 1,
     *   "receiverAgentId": 2,
     *   "sessionId": "sess-001",
     *   "content": "你好，请帮我起草一份劳动合同。",
     *   "extraParams": {"temperature": 0.7, "maxTokens": 1024}
     * }
     *
     * 响应JSON：
     * { "reply": "模型输出文本..." }
     */
    @PostMapping("/private")
    public ResponseEntity<Map<String, Object>> sendPrivate(@RequestBody Map<String, Object> body) {
        Long senderAgentId = ((Number) body.get("senderAgentId")).longValue();
        Long receiverAgentId = ((Number) body.get("receiverAgentId")).longValue();
        String sessionId = (String) body.get("sessionId");
        String content = (String) body.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> extraParams = (Map<String, Object>) body.getOrDefault("extraParams", Map.of());

        String reply = conversationService.sendPrivateMessage(senderAgentId, receiverAgentId, sessionId, content, extraParams);
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    /**
     * 群组讨论：广播消息并返回所有参与者的回复
     *
     * 请求JSON：
     * {
     *   "roomId": "room-001",
     *   "senderAgentId": 1,
     *   "participantIds": [2,3,4],
     *   "content": "请大家基于各自专业给出方案。",
     *   "extraParams": {"temperature": 0.6}
     * }
     *
     * 响应JSON：
     * { "summary": "Agent 2 回复：...\nAgent 3 回复：...\n..." }
     */
    @PostMapping("/room")
    public ResponseEntity<Map<String, Object>> broadcastRoom(@RequestBody Map<String, Object> body) {
        String roomId = (String) body.get("roomId");
        Long senderAgentId = body.get("senderAgentId") == null ? null : ((Number) body.get("senderAgentId")).longValue();
        @SuppressWarnings("unchecked")
        List<Number> participantIdsNum = (List<Number>) body.get("participantIds");
        List<Long> participantIds = participantIdsNum.stream().map(Number::longValue).toList();
        String content = (String) body.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> extraParams = (Map<String, Object>) body.getOrDefault("extraParams", Map.of());

        String summary = conversationService.broadcastRoomMessage(roomId, senderAgentId, participantIds, content, extraParams);
        return ResponseEntity.ok(Map.of("summary", summary));
    }
}
