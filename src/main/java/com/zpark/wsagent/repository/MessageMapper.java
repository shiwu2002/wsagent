package com.zpark.wsagent.repository;

import com.github.yulichang.base.MPJBaseMapper;
import com.zpark.wsagent.domain.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper（MyBatis-Plus-Join）
 */
@Mapper
public interface MessageMapper extends MPJBaseMapper<Message> {
}
