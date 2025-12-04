-- 创建聊天消息表（MySQL）
CREATE TABLE IF NOT EXISTS chat_messages (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  type VARCHAR(32) NOT NULL COMMENT '消息类型：GROUP_MSG/PRIVATE_MSG',
  from_user_id VARCHAR(128) NOT NULL COMMENT '发送方用户ID',
  to_user_id VARCHAR(128) NULL COMMENT '私聊目标用户ID（群聊为空）',
  room_id VARCHAR(128) NULL COMMENT '群聊房间ID（私聊为空）',
  content TEXT NOT NULL COMMENT '消息内容',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_room_time (room_id, created_at),
  KEY idx_private_pair_time (from_user_id, to_user_id, created_at),
  KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';


CREATE TABLE IF NOT EXISTS ai_model_info (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  model_factory VARCHAR(100) NOT NULL COMMENT '模型厂家',
  model_name VARCHAR(200) NOT NULL COMMENT '模型名称',
  model_context_prompt TEXT COMMENT '模型背景提示词',
  model_system_prompt TEXT COMMENT '模型系统提示词',
  creator VARCHAR(50) NOT NULL COMMENT '创建人',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除字段：0-未删除，1-已删除',
  PRIMARY KEY (id),
  KEY idx_model_factory (model_factory),
  KEY idx_model_name (model_name),
  KEY idx_creator (creator),
  KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型信息表';