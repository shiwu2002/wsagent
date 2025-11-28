package com.zpark.wsagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zpark.wsagent.emtity.ChatMessageEntity;

import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息表的 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
}
