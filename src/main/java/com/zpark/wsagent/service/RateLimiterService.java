package com.zpark.wsagent.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 简易限流服务（基于Redis计数窗口）
 *
 * - 维度：按客户端IP + 接口Key
 * - 策略：固定窗口计数（windowSeconds），超过 maxRequests 拒绝
 * - 键格式：ratelimit:{key}:{ip}
 */
@Service
public class RateLimiterService {

    private final StringRedisTemplate redis;

    public RateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * 检查是否允许访问，若首次访问则初始化计数与过期时间
     * @param ip 客户端IP
     * @param key 接口Key（如 "api:conversation:private"）
     * @param windowSeconds 窗口秒数
     * @param maxRequests 窗口内最大请求次数
     * @return 是否允许
     */
    public boolean allow(String ip, String key, int windowSeconds, int maxRequests) {
        String k = "ratelimit:" + key + ":" + ip;
        Long v = redis.opsForValue().increment(k);
        if (v != null && v == 1L) {
            redis.expire(k, Duration.ofSeconds(windowSeconds));
        }
        return v != null && v <= maxRequests;
    }
}
