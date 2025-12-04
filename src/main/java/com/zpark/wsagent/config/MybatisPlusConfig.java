package com.zpark.wsagent.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：
 * - 分页拦截器
 * - Mapper 扫描
 */
@Configuration
@MapperScan("com.zpark.wsagent.mapper")
public class MybatisPlusConfig {

    /**
     * 注册MyBatis-Plus分页拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mp = new MybatisPlusInterceptor();
        mp.addInnerInterceptor(new PaginationInnerInterceptor());
        return mp;
    }
}
