package com.zpark.wsagent.enums;

/**
 * WebSocket意图类型枚举
 * 用于标识不同的WebSocket操作方法
 */
public enum IntentType {
    
    /**
     * 加入群组
     */
    JOIN_GROUP,
    
    /**
     * 发送消息
     */
    SEND_MESSAGE,
    
    /**
     * 离开群组
     */
    LEAVE_GROUP,
    
    /**
     * 创建房间
     */
    CREATE_ROOM,
    
    /**
     * 删除房间
     */
    DELETE_ROOM,
    
    /**
     * 私聊
     */
    PRIVATE_CHAT,
    
    /**
     * 公共聊天
     */
    PUBLIC_CHAT,
    
    /**
     * 心跳检测
     */
    HEARTBEAT,
    
    /**
     * 用户状态更新
     */
    UPDATE_STATUS,
    
    /**
     * 获取在线用户列表
     */
    GET_ONLINE_USERS, 

    /**
     * 断开私聊连接
     */
    DISCONNECT_PRIVATE_CHAT;


    /**
     * 根据字符串获取对应的枚举值
     * @param value 字符串值
     * @return 对应的枚举值，找不到则返回null
     */
    public static IntentType fromString(String value) {
        for (IntentType type : IntentType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}