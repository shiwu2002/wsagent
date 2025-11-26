# 前端集成交付文档（React 前端）

目标：为 React(前后端分离) 提供明确的 API 合同、数据结构、运行流程与示例，便于快速生成前端界面与交互逻辑。

注意事项：
- 后端已开启全局 CORS(跨域)，默认允许 http://localhost:3000。生产环境请在 CorsConfig 中调整允许域名。
- 所有接口均为 JSON(JavaScript Object Notation) 输入/输出。
- 未实现鉴权（Auth），如需登录鉴权请在网关或控制器层补充。

————————————
一、核心概念与数据模型

1) 智能体(Agent)
- 字段（参考后端实体/表）：
  - id(Long)：智能体ID
  - name(String)：名称
  - roleId(Long)：绑定的角色ID
  - modelType(String)：模型类型枚举字符串，如 "OLLAMA"、"QWEN"、"DASH_SCOPE"、"MODELSCOPE"
  - modelName(String)：具体模型名称，如 "qwen-turbo"、"llama3.1:8b"
  - description(String)：描述（可选）

2) 角色(Role)
- 字段：
  - id(Long)
  - name(String)
  - description(String)
  - systemMemory(Text)：系统记忆（固定身份设定、专业背景、性格特征或知识边界）

3) 消息(Message)
- 字段（前端关心的核心字段）：
  - id(Long)
  - senderAgentId(Long)：发送者智能体ID
  - receiverAgentId(Long|null)：接收者智能体ID（公共池为 null）
  - sessionId(String|null)：私聊会话ID
  - roomId(String|null)：讨论室ID（公共池约定为 "PUBLIC"）
  - content(String)：消息文本
  - modelType(String|null)：生成回复所用模型类型（OUT 消息可能携带）
  - modelName(String|null)：生成回复所用模型名称（OUT 消息可能携带）
  - roleId(Long|null)：生成方绑定角色ID（OUT 消息可能携带）
  - direction(String)："IN"（外部输入/来信）或 "OUT"（模型生成/回复）
  - createdAt(Long)：时间戳（epoch 毫秒）
  - roundId(Long|null)：所属运行回合ID
  - isAutonomous(Boolean|null)：是否为自主行为（true/false）
  - metadata(String|JSON|null)：扩展元数据（例如温度、top_p等）

4) 动态记忆缓存（Redis，后端内部）
- Key 约定：
  - agent:{agentId}:session:{sessionId}（点对点会话窗口）
  - agent:{agentId}:room:{roomId}（房间窗口）
- 前端无需直接访问 Redis，由后端服务读写。

————————————
二、接口合同（REST API）

Base URL 示例：
- 开发环境（本地）：http://localhost:8080
- 前端通过 fetch/axios 调用，以 /api/* 为路径前缀

1) 会话协作接口（被动对话 & 群组讨论）

POST /api/conversation/private
- 用途：发送一条私聊消息，并获得接收者智能体的模型回复（单轮）
- 请求体(JSON)：
  {
    "senderAgentId": 1,
    "receiverAgentId": 2,
    "sessionId": "s-1-2",
    "content": "你好，请帮我分析这份报告。",
    "extraParams": {
      "temperature": 0.7,
      "maxTokens": 1024,
      "roundId": 1001
    }
  }
- 响应体(JSON)：
  { "reply": "模型输出文本..." }

POST /api/conversation/room
- 用途：在讨论室广播消息，房间内每个参与者智能体各自生成回复并落库
- 请求体(JSON)：
  {
    "roomId": "room-001",
    "senderAgentId": 1,
    "participantIds": [2, 3, 4],
    "content": "请大家基于各自专业给出方案。",
    "extraParams": {
      "temperature": 0.6,
      "roundId": 1002
    }
  }
- 响应体(JSON)：
  { "summary": "Agent 2 回复：...\nAgent 3 回复：...\nAgent 4 回复：..." }

2) 主动行为运行接口（协作回合）

POST /api/round/run
- 用途：触发一轮“协作回合”，系统为每个参与的智能体执行“感知-思考-行动”
- 请求体(JSON)：
  { "participantIds": [1, 2, 3], "roundId": 1001 }
- 响应体(JSON)：
  { "status": "ok", "roundId": 1001 }

POST /api/round/run-batch
- 用途：连续触发多轮（例如 3 轮）
- 请求体(JSON)：
  { "participantIds": [1, 2, 3], "startRoundId": 2000, "rounds": 3 }
- 响应体(JSON)：
  { "status": "ok", "roundsRun": 3 }

3) 消息查询接口（为 React 前端提供数据源）

GET /api/messages/public?limit=10
- 用途：拉取公共消息池（roomId="PUBLIC"）最近 n 条
- 响应体：
  { "items": [Message, ...] }

GET /api/messages/private/{agentId}?limit=10
- 用途：拉取指定智能体的私信收件箱（仅 direction="IN" 消息）
- 响应体：
  { "items": [Message, ...] }

GET /api/messages/room/{roomId}?limit=20
- 用途：拉取指定讨论室最近消息
- 响应体：
  { "items": [Message, ...] }

GET /api/messages/session/{sessionId}?limit=50
- 用途：拉取指定点对点会话线程
- 响应体：
  { "items": [Message, ...] }

GET /api/messages/public/by-round?roundId=1001&limit=20
- 用途：按回合ID过滤公共池消息
- 响应体：
  { "items": [Message, ...] }

————————————
三、主动行为机制（LLM 指令协议）

后端为主动运行构造 Prompt 示例片段：
[系统记忆]
你是一位资深医学研究员，专注于传染病建模...

[当前任务]
请根据以下信息决定是否采取行动。你可以：1) 在公共池发言，2) 私信某专家，3) 回复特定消息，4) 保持沉默。

[最近对话历史]
- ...

[公共消息池最新摘要]
- ...

[私信待处理]
- ...

LLM 输出期望的 JSON 指令：
{
  "action": "public_post" | "private_message" | "reply" | "silence",
  "target": "agent_03 或 目标智能体ID(可选)",
  "content": "要发表/发送的文本"
}

后端执行策略：
- public_post：写入公共池（roomId="PUBLIC"），仅入库，不触发其它 Agent 立即回应
- private_message：调用 /api/conversation/private 的内部逻辑，向目标 Agent 发起私聊并获得其模型回复
- reply：若 target 是某个智能体，则私信回复；否则回退为公共池回复
- silence：不发言

落库标注：
- roundId：本轮回合ID
- isAutonomous：public_post/private_message/reply 等动作生成的“OUT”消息标记为 true；前置“IN”消息标记为 false

————————————
四、React 前端集成建议

1) 页面模块
- 公共池时间线（Timeline）
  - 数据源：GET /api/messages/public 或 /api/messages/public/by-round?roundId=*
  - 展示字段：senderAgentId、content、createdAt、roundId、isAutonomous
- 私信面板（Inbox & Threads）
  - 数据源：GET /api/messages/private/{agentId} 拉取收件箱，点击某一条进入会话：GET /api/messages/session/{sessionId}
- 回合控制面板（Rounds）
  - 触发单轮：POST /api/round/run
  - 批量多轮：POST /api/round/run-batch
- 讨论室视图（Rooms）
  - 数据源：GET /api/messages/room/{roomId}
  - 发送广播：POST /api/conversation/room

2) 示例代码（使用 fetch）
- 触发回合：
  fetch("/api/round/run", {
    method: "POST",
    headers: {"Content-Type":"application/json"},
    body: JSON.stringify({participantIds:[1,2,3], roundId: 1001})
  }).then(r => r.json()).then(d => console.log(d));

- 私聊并获得回复：
  fetch("/api/conversation/private", {
    method: "POST",
    headers: {"Content-Type":"application/json"},
    body: JSON.stringify({
      senderAgentId: 1,
      receiverAgentId: 2,
      sessionId: "s-1-2",
      content: "你好，请分析这份报告",
      extraParams: { roundId: 1001, temperature: 0.7 }
    })
  }).then(r => r.json()).then(d => console.log(d.reply));

- 拉取公共池：
  fetch("/api/messages/public?limit=10")
    .then(r => r.json())
    .then(d => setFeed(d.items));

3) 状态管理与数据刷新
- 建议使用 Redux 或 Zustand 管理当前选中房间/会话与最近消息列表。
- 回合运行结束后刷新相关视图（公共池/私信/回合过滤）。

4) 错误处理与边界
- 所有接口可能返回 4xx/5xx 错误；前端应统一拦截并提示。
- 字段类型严格按合同转换（例如 Number → Long）。
- limit 参数有最大值保护（公共池 ≤100、房间 ≤200、会话 ≤500）。

————————————
五、环境配置

1) CORS
- 后端允许 http://localhost:3000 跨域访问；生产环境请在 CorsConfig 中增加/替换 allowedOriginPattern。

2) 后端运行
- 命令：./mvnw spring-boot:run
- 端口：默认 8080（可在 application.properties 中配置）

3) LLM 接入与鉴权
- DashScope 与 ModelScope 的 API Key/URL 请在 application.properties 或环境变量中配置，并通过 LlmRequest.extraParams 透传到客户端适配层（如 modelscopeBaseUrl、modelscopeApiKey）。

————————————
六、待扩展项（前端可预留）

- 角色与智能体管理页面（CRUD）
- 回合历史浏览与复盘（按 roundId 聚合视图）
- 公共池摘要与私信待处理摘要的服务端实现（AutonomousRunnerService 中目前为占位方法）
- 实时推送：如需实时更新，可在后端添加 WebSocket/SSE 推送，前端订阅消息流。

————————————
七、交付说明

- 本文档即为前端生成的基础规范。前端可据此搭建页面与调用接口。
- 如需新增接口或字段，请在此文档更新并与后端同步修改。
