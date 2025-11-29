package com.zpark.wsagent.service.impl;

import com.zpark.wsagent.service.ChatConnectService;
import com.zpark.wsagent.websocket.ChatSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 聊天连接服务实现类
 * 提供加入/离开群聊以及私聊连接管理功能的具体实现
 */
@Service
public class ChatConnectServiceImpl implements ChatConnectService {

    private final ChatSessionRegistry chatSessionRegistry;

    public ChatConnectServiceImpl(ChatSessionRegistry chatSessionRegistry) {
        this.chatSessionRegistry = chatSessionRegistry;
    }

    /**
     * 加入群聊
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    @Override
    public void joinGroup(String roomId, String userId) {
        chatSessionRegistry.joinRoom(roomId, userId);
    }

    /**
     * 离开群聊
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    @Override
    public void leaveGroup(String roomId, String userId) {
        chatSessionRegistry.leaveRoom(roomId, userId);
    }

    /**
     * 获取群聊成员列表
     *
     * @param roomId 房间ID
     * @return 群聊成员ID集合
     */
    @Override
    public Set<String> getGroupMembers(String roomId) {
        return chatSessionRegistry.getRoomMembers(roomId);
    }

    /**
     * 建立私聊连接
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     */
    @Override
    public void connectPrivateChat(String userId, String targetUserId) {
        // 私聊不需要特殊的连接操作，只需要确保双方都在线即可
        // 这里可以记录私聊连接状态或其他相关操作
    }

    /**
     * 断开私聊连接
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     */
    @Override
    public void disconnectPrivateChat(String userId, String targetUserId) {
        // 私聊断开连接的清理工作
    }
}