package com.zpark.wsagent.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 系统记忆缓存服务（Redis）
 *
 * - 缓存角色的系统记忆文本，减少数据库读取压力
 * - Key：role:system:{roleId}
 * - TTL 可选（默认不设置过期）
 */
@Service
public class SystemMemoryCacheService {

    private final StringRedisTemplate redis;

    public SystemMemoryCacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 生成缓存键 */
    public String key(Long roleId) {
        return "role:system:" + roleId;
    }

    /** 从缓存获取系统记忆（命中返回字符串，未命中返回 null） */
    public String get(Long roleId) {
        if (roleId == null) return null;
        return redis.opsForValue().get(key(roleId));
    }

    /** 写入缓存（可选设置过期时间） */
    public void put(Long roleId, String systemMemory, Long ttlSeconds) {
        if (roleId == null) return;
        String k = key(roleId);
        if (ttlSeconds != null && ttlSeconds > 0) {
            redis.opsForValue().set(k, systemMemory, Duration.ofSeconds(ttlSeconds));
        } else {
            redis.opsForValue().set(k, systemMemory);
        }
    }

    /** 删除缓存 */
    public void evict(Long roleId) {
        if (roleId == null) return;
        redis.delete(key(roleId));
    }
}
