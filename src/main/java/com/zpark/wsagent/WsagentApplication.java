package com.zpark.wsagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口
 * - 开启 Mapper 扫描，支持 MyBatis-Plus-Join 的 Mapper 接口
 */
@SpringBootApplication
@MapperScan("com.zpark.wsagent.repository")
public class WsagentApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsagentApplication.class, args);
	}

}
