/*
  初始化数据库表结构（MySQL）
  - 使用 MyBatis-Plus 注解实体，无需 XML，即可通过 MPJBaseMapper CRUD
  - 包含：roles、agents、messages 三张表
  - 可根据需要调整字段类型与索引
*/

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(512),
  system_memory TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS agents (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  role_id BIGINT,
  model_type VARCHAR(64),   -- 与 LlmClient.ModelType 对应的字符串，如 "OLLAMA"、"QWEN"
  model_name VARCHAR(128),  -- 具体模型名称，如 "qwen3:8b"
  description VARCHAR(512),
  CONSTRAINT fk_agents_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_agent_id BIGINT,
  receiver_agent_id BIGINT,
  session_id VARCHAR(128),
  room_id VARCHAR(128),
  content TEXT,
  model_type VARCHAR(64),
  model_name VARCHAR(128),
  role_id BIGINT,
  direction VARCHAR(8),     -- IN / OUT
  created_at BIGINT,        -- epoch 毫秒
  round_id BIGINT,          -- 所属协作回合ID
  is_autonomous TINYINT(1), -- 是否为自主行为：1=true, 0=false
  metadata JSON NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 索引优化（根据典型查询模式添加）
CREATE INDEX IF NOT EXISTS idx_messages_session ON messages (session_id);
CREATE INDEX IF NOT EXISTS idx_messages_room ON messages (room_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages (sender_agent_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON messages (receiver_agent_id);
CREATE INDEX IF NOT EXISTS idx_messages_round ON messages (round_id);
CREATE INDEX IF NOT EXISTS idx_messages_autonomous ON messages (is_autonomous);
