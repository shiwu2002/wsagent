package com.zpark.wsagent.llm;

import java.util.*;

/**
 * 多模型适配统一接口：LlmClient
 *
 * 设计目标：
 * - 以统一的接口封装不同大模型（中文通用名称+具体提供方），实现插件式接入
 * - 将“系统记忆(System Memory)”与“动态对话记忆(Dynamic Memory)”在调用前统一拼接
 * - 支持多会话隔离（私聊会话ID）与讨论室隔离（讨论室ID）
 * - 允许透传模型参数（温度、最大token、top_p等），即“可扩展的可选参数”
 *
 * 使用方式：
 * - 通过实现类（如 DashScopeLlmClient、OllamaLlmClient、ModelScopeLlmClient、OpenAiLlmClient、ClaudeLlmClient 等）
 *   实现 generate(LlmRequest) 即可完成具体模型调用。
 * - 业务层（如 ConversationService）在从Redis读取动态记忆、从MySQL读取系统记忆后，组装 LlmRequest 调用此接口。
 *
 * 约定：
 * - prompt构造采用“系统记忆 + 动态记忆窗口 + 用户消息”的顺序
 * - 如需更复杂的消息结构（如多模态、工具调用），可扩展 LlmRequest.extraParams 携带模型特定payload
 */
public interface LlmClient {

    /**
     * 生成回复（核心调用入口）
     * @param request LlmRequest 请求参数，包含模型类型/名称、系统记忆、动态记忆窗口、用户消息、扩展参数等
     * @return 大模型生成的回复文本
     */
    String generate(LlmRequest request);

    /**
     * 默认的 prompt 构造器：按“系统记忆 → 动态记忆窗口 → 用户消息”顺序拼接。
     * 不同模型的实现类可选择使用或覆盖此逻辑。
     *
     * @param systemMemory 系统记忆（固定、不可被覆盖）
     * @param dynamicMemory 动态对话记忆（最近N轮对话摘要或原文）
     * @param userMessage 当前用户输入
     * @return 拼接后的完整上下文文本
     */
    default String buildPrompt(String systemMemory, List<String> dynamicMemory, String userMessage) {
        StringBuilder sb = new StringBuilder();
        if (systemMemory != null && !systemMemory.isEmpty()) {
            sb.append("【系统记忆】\n").append(systemMemory.trim()).append("\n\n");
        }
        if (dynamicMemory != null && !dynamicMemory.isEmpty()) {
            sb.append("【上下文记忆（最近N轮）】\n");
            for (String mem : dynamicMemory) {
                if (mem != null && !mem.isEmpty()) {
                    sb.append("- ").append(mem.trim()).append("\n");
                }
            }
            sb.append("\n");
        }
        if (userMessage != null && !userMessage.isEmpty()) {
            sb.append("【当前问题】\n").append(userMessage.trim());
        }
        return sb.toString();
    }

    /**
     * 模型类型（抽象层枚举）
     * - QWEN(通义千问)、GPT_4(OpenAI)、CLAUDE(Anthropic)、OLLAMA(本地推理)、DASH_SCOPE(阿里达摩院)、MODELSCOPE(魔搭平台) 等
     * - 如需扩展其它提供方，可在此枚举中追加
     */
    enum ModelType {
        QWEN,
        GPT_4,
        CLAUDE,
        OLLAMA,
        DASH_SCOPE,
        MODELSCOPE
    }

    /**
     * LLM 请求载体
     * 注意：因为接口中的嵌套类型在Java中默认是public static的，因此可以直接使用。
     */
    class LlmRequest {
        /**
         * 模型类型（统一枚举，不同提供方使用此字段区分）
         */
        private ModelType modelType;

        /**
         * 具体模型名称（中文或英文标识，例如：
         * - "qwen-turbo"（通义千问）
         * - "gpt-4o-mini"（OpenAI）
         * - "claude-3-5-sonnet"（Anthropic）
         * - "llama3.1:8b"（Ollama）
         * - "dashscope-qwen-plus"（达摩院）
         * - "modelscope-qwen2.5"（魔搭）
         * ）
         */
        private String modelName;

        /**
         * 角色系统记忆（System Memory）：固定文本，不被对话覆盖
         */
        private String systemMemory;

        /**
         * 动态记忆窗口（最近N轮的消息摘要或原文）
         */
        private List<String> dynamicMemory;

        /**
         * 当前用户输入（需要生成回复的内容）
         */
        private String userMessage;

        /**
         * 会话隔离信息：
         * - 私聊会话ID（点对点）
         * - 讨论室ID（群聊）
         */
        private String sessionId;
        private String roomId;

        /**
         * 关联的智能体ID（用于区分不同角色、模型绑定）
         */
        private Long agentId;

        /**
         * 可扩展的模型调用参数：温度(temperature)、最大token(maxTokens)、top_p、工具列表、函数签名等
         * 约定键： "temperature" (Double), "maxTokens" (Integer), "topP" (Double), ...
         */
        private Map<String, Object> extraParams;

        public LlmRequest() {
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getter / Setter
        public ModelType getModelType() {
            return modelType;
        }

        public void setModelType(ModelType modelType) {
            this.modelType = modelType;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getSystemMemory() {
            return systemMemory;
        }

        public void setSystemMemory(String systemMemory) {
            this.systemMemory = systemMemory;
        }

        public List<String> getDynamicMemory() {
            return dynamicMemory;
        }

        public void setDynamicMemory(List<String> dynamicMemory) {
            this.dynamicMemory = dynamicMemory;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public void setUserMessage(String userMessage) {
            this.userMessage = userMessage;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public Long getAgentId() {
            return agentId;
        }

        public void setAgentId(Long agentId) {
            this.agentId = agentId;
        }

        public Map<String, Object> getExtraParams() {
            return extraParams;
        }

        public void setExtraParams(Map<String, Object> extraParams) {
            this.extraParams = extraParams;
        }

        /**
         * 便捷读取常用参数（若不存在则返回默认）
         */
        public double temperature(double def) {
            Object v = (extraParams == null) ? null : extraParams.get("temperature");
            return (v instanceof Number) ? ((Number) v).doubleValue() : def;
        }

        public int maxTokens(int def) {
            Object v = (extraParams == null) ? null : extraParams.get("maxTokens");
            return (v instanceof Number) ? ((Number) v).intValue() : def;
        }

        public double topP(double def) {
            Object v = (extraParams == null) ? null : extraParams.get("topP");
            return (v instanceof Number) ? ((Number) v).doubleValue() : def;
        }

        /**
         * 构建器（Builder）
         */
        public static class Builder {
            private final LlmRequest req = new LlmRequest();

            public Builder modelType(ModelType modelType) {
                req.setModelType(modelType);
                return this;
            }

            public Builder modelName(String modelName) {
                req.setModelName(modelName);
                return this;
            }

            public Builder systemMemory(String systemMemory) {
                req.setSystemMemory(systemMemory);
                return this;
            }

            public Builder dynamicMemory(List<String> dynamicMemory) {
                req.setDynamicMemory(dynamicMemory);
                return this;
            }

            public Builder userMessage(String userMessage) {
                req.setUserMessage(userMessage);
                return this;
            }

            public Builder sessionId(String sessionId) {
                req.setSessionId(sessionId);
                return this;
            }

            public Builder roomId(String roomId) {
                req.setRoomId(roomId);
                return this;
            }

            public Builder agentId(Long agentId) {
                req.setAgentId(agentId);
                return this;
            }

            public Builder extraParams(Map<String, Object> extraParams) {
                req.setExtraParams(extraParams);
                return this;
            }

            public LlmRequest build() {
                // 保证非空集合
                if (req.getDynamicMemory() == null) {
                    req.setDynamicMemory(new ArrayList<>());
                }
                if (req.getExtraParams() == null) {
                    req.setExtraParams(new HashMap<>());
                }
                return req;
            }
        }
    }
}
