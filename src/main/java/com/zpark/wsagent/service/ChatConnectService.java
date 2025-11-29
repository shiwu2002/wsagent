package com.zpark.wsagent.service;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 聊天连接服务接口
 * 提供加入/离开群聊以及私聊连接管理功能
 */
@Service
public interface ChatConnectService {

    /**
     * 加入群聊
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void joinGroup(String roomId, String userId);

    /**
     * 离开群聊
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    void leaveGroup(String roomId, String userId);

    /**
     * 获取群聊成员列表
     *
     * @param roomId 房间ID
     * @return 群聊成员ID集合
     */
    Set<String> getGroupMembers(String roomId);

    /**
     * 建立私聊连接
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     */
    void connectPrivateChat(String userId, String targetUserId);

    /**
     * 断开私聊连接
     *
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     */
    void disconnectPrivateChat(String userId, String targetUserId);
}