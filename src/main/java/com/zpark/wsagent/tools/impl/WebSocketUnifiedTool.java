package com.zpark.wsagent.tools.impl;

import com.zpark.wsagent.enums.IntentType;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zpark.wsagent.dto.ToolMessage;
import com.zpark.wsagent.service.ChatConnectService;
import com.zpark.wsagent.tools.ToolBase;

import java.util.logging.Logger;

@Component
public class WebSocketUnifiedTool implements ToolBase {

    private static final Logger logger = Logger.getLogger(WebSocketUnifiedTool.class.getName());

    @Autowired
    private ChatConnectService chatConnectService;


    @Override
    public String apply(ToolMessage t, ToolContext toolContext) {
        try {
            if (t == null || t.getIntentType() == null) {
                // 默认处理
                if (t != null) {
                    handleDefaultIntent(t);
                }
            } else {
                // 根据意图类型处理不同操作
                handleIntent(t);
            }
        } catch (Exception e) {
            logger.severe("Error occurred while processing WebSocket intent: " + e.getMessage());
            e.printStackTrace(); // 生产环境中推荐移除此行并依赖日志系统
            return "Failed due to internal error";
        }

        return "Success";
    }

    /**
     * 根据意图类型处理不同的WebSocket操作
     * @param t 工具消息对象
     */
    private void handleIntent(ToolMessage t) {
        IntentType intent = t.getIntentType();
        String roomId = t.getRoomId();
        String senderId = t.getSenderId();
        String targetUserId = t.getTargetUserId();

        if (senderId == null) {
            logger.warning("Missing sender ID in tool message");
            return;
        }

        switch (intent) {
            case JOIN_GROUP:
                if (roomId == null) {
                    logger.warning("Missing room ID for JOIN_GROUP operation");
                    return;
                }
                chatConnectService.joinGroup(roomId, senderId);
                break;
            case LEAVE_GROUP:
                if (roomId == null) {
                    logger.warning("Missing room ID for LEAVE_GROUP operation");
                    return;
                }
                chatConnectService.leaveGroup(roomId, senderId);
                break;
            case PRIVATE_CHAT:
                if (targetUserId == null) {
                    logger.warning("Missing target user ID for PRIVATE_CHAT operation");
                    return;
                }
                chatConnectService.connectPrivateChat(senderId, targetUserId);
                break;
            case GET_ONLINE_USERS:
                if (roomId == null) {
                    logger.warning("Missing room ID for GET_ONLINE_USERS operation");
                    return;
                }
                chatConnectService.getGroupMembers(roomId);
                break;
            case DISCONNECT_PRIVATE_CHAT:
                if (targetUserId == null) {
                    logger.warning("Missing target user ID for DISCONNECT_PRIVATE_CHAT operation");
                    return;
                }
                chatConnectService.disconnectPrivateChat(senderId, targetUserId);
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
            logger.info("Executing default action: joining group.");
            chatConnectService.joinGroup(t.getRoomId(), t.getSenderId());
        } else {
            logger.warning("Default handler skipped due to missing roomId or senderId.");
        }
    }

    @Override
    public String toolName() {
        return "websocket_unified_tool";
    }

    @Override
    public String ToolDescription() {
        return "Unified tool for handling various WebSocket operations. Supports multiple intents like JOIN_GROUP, LEAVE_GROUP, PRIVATE_CHAT, GET_ONLINE_USERS, DISCONNECT_PRIVATE_CHAT, etc.";
    }

    @Override
    public ToolCallback getToolCallback() {
        return FunctionToolCallback.builder(toolName(), this)
                .description(ToolDescription())
                .inputType(ToolMessage.class)
                .build();
    }
}
