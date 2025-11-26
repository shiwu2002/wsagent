package com.zpark.wsagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 全局CORS配置，支持前后端分离（React前端）跨域请求
 *
 * - 允许从本地React开发服务器（http://localhost:3000）发起跨域
 * - 允许常见HTTP方法与凭证（如有需要）
 * - 生产环境可将allowedOrigins替换为具体域名
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 根据实际前端地址调整，开发阶段通常是 http://localhost:3000
        cfg.addAllowedOriginPattern("http://localhost:3000");
        // 如果需要支持多个环境域名，可追加：
        // cfg.addAllowedOriginPattern("https://your-react-domain.com");

        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("GET");
        cfg.addAllowedMethod("POST");
        cfg.addAllowedMethod("PUT");
        cfg.addAllowedMethod("DELETE");
        cfg.addAllowedMethod("OPTIONS");
        // 如需携带cookie等凭证：
        cfg.setAllowCredentials(true);
        // 预检缓存时长（秒）
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
