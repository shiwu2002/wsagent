package com.zpark.wsagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpark.wsagent.emtity.AiModelInfoEntity;

import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型信息表 Mapper
 */
@Mapper
public interface AiModelInfoMapper extends BaseMapper<AiModelInfoEntity> {
}
