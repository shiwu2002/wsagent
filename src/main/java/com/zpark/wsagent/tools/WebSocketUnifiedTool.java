package com.zpark.wsagent.tools;

import com.zpark.wsagent.enums.IntentType;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zpark.wsagent.dto.ToolMessage;
import com.zpark.wsagent.service.ChatConnectService;

@Component
public class WebSocketUnifiedTool implements ToolBase {

    @Autowired
    private ChatConnectService chatConnectService;

    @Override
    public ToolCallback apply(ToolMessage t, ToolContext toolContext) {
        try {
            if (t == null || t.getIntentType() == null) {
                // 默认处理
                handleDefaultIntent(t);
            } else {
                // 根据意图类型处理不同操作
                handleIntent(t);
            }
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
        }

        return FunctionToolCallback
                .builder(toolName(), this)
                .description(ToolDescription())
                .inputType(ToolMessage.class)
                .build();
    }

    /**
     * 根据意图类型处理不同的WebSocket操作
     * @param t 工具消息对象
     */
    private void handleIntent(ToolMessage t) {
        IntentType intent = t.getIntentType();
        switch (intent) {
            case JOIN_GROUP:
                chatConnectService.joinGroup(t.getRoomId(), t.getSenderId());
                break;
            case LEAVE_GROUP:
                chatConnectService.leaveGroup(t.getRoomId(), t.getSenderId());
                break;
            case PRIVATE_CHAT:
                chatConnectService.connectPrivateChat(t.getSenderId(), t.getTargetUserId());
                break;
            case GET_ONLINE_USERS:
                chatConnectService.getGroupMembers(t.getRoomId());
                break;
            case DISCONNECT_PRIVATE_CHAT:
                chatConnectService.disconnectPrivateChat(t.getSenderId(), t.getTargetUserId());
                break;
        
            // 其他意图可以根据需要扩展
            default:
                handleDefaultIntent(t);
                break;
        }
    }

    /**
     * 默认处理方法
     * @param t 工具消息对象
     */
    private void handleDefaultIntent(ToolMessage t) {
        // 默认处理逻辑，可以根据实际需求进行调整
        if (t.getRoomId() != null && t.getSenderId() != null) {
            chatConnectService.joinGroup(t.getRoomId(), t.getSenderId());
        }
    }

    @Override
    public String toolName() {
        return "websocket_unified_tool";
    }

    @Override
    public String ToolDescription() {
        return "Unified tool for handling various WebSocket operations. Supports multiple intents like JOIN_GROUP, LEAVE_GROUP, PRIVATE_CHAT, GET_ONLINE_USERS, DISCONNECT_PRIVATE_CHAT, BROADCAST_MESSAGE, KICK_USER, MUTE_USER, UNMUTE_USER, etc.";
    }
}