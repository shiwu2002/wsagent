package com.zpark.wsagent.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 动态对话记忆窗口服务（Redis）
 *
 * - 使用 Redis 列表(List)保存最近 N 条消息摘要/原文
 * - Key 设计：
 *   * 私聊：agent:{agentId}:session:{sessionId}
 *   * 群聊：room:{roomId}:agent:{agentId}
 * - 提供追加、读取、清理等接口
 */
@Service
public class MemoryService {

    private final StringRedisTemplate redis;

    /** 记忆窗口长度（最近 N 条） */
    @Value("${app.memory.window-length:10}")
    private int windowLength;

    /** 可选：为窗口设置过期时间，避免长期堆积（秒） */
    @Value("${app.memory.ttl-seconds:0}")
    private long ttlSeconds;

    public MemoryService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 构造私聊Key */
    public String keyForSession(Long agentId, String sessionId) {
        return "agent:" + agentId + ":session:" + sessionId;
    }

    /** 构造群聊Key（按讨论室与Agent维度隔离） */
    public String keyForRoom(Long agentId, String roomId) {
        return "room:" + roomId + ":agent:" + agentId;
    }

    /**
     * 追加一条记忆到窗口（列表右端），超出窗口后裁剪
     * @param key Redis键（建议使用 keyForSession / keyForRoom 生成）
     * @param memory 文本摘要或原文
     */
    public void append(String key, String memory) {
        redis.opsForList().rightPush(key, memory);
        // 仅保留最近 N 条
        redis.opsForList().trim(key, -windowLength, -1);
        // 可选过期
        if (ttlSeconds > 0) {
            redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取窗口内的所有记忆（按时间顺序）
     */
    public List<String> getWindow(String key) {
        Long size = redis.opsForList().size(key);
        if (size == null || size == 0) {
            return List.of();
        }
        return redis.opsForList().range(key, Math.max(0, size - windowLength), size - 1);
    }

    /**
     * 清理指定键的窗口
     */
    public void clear(String key) {
        redis.delete(key);
    }

    /**
     * 更新窗口长度（运行时调整）
     */
    public void setWindowLength(int windowLength) {
        if (windowLength <= 0) {
            throw new IllegalArgumentException("windowLength 必须为正整数");
        }
        this.windowLength = windowLength;
    }

    public int getWindowLength() {
        return windowLength;
    }
}
