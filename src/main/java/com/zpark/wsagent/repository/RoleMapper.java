package com.zpark.wsagent.repository;

import com.github.yulichang.base.MPJBaseMapper;
import com.zpark.wsagent.domain.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表 Mapper（MyBatis-Plus-Join）
 */
@Mapper
public interface RoleMapper extends MPJBaseMapper<Role> {
}
