package com.zpark.wsagent.repository;

import com.github.yulichang.base.MPJBaseMapper;
import com.zpark.wsagent.domain.Agent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能体（Agent）表 Mapper（MyBatis-Plus-Join）
 */
@Mapper
public interface AgentMapper extends MPJBaseMapper<Agent> {
}
