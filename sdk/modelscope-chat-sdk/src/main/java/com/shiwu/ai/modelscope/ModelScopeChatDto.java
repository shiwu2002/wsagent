package com.shiwu.ai.modelscope;

import java.util.List;

/**
 * ModelScope/OpenAI 兼容的请求/响应数据结构（SDK）
 * 仅用于封装 HTTP 交互的数据模型，便于与 WebClient 解耦。
 * 
 * 说明：该类从业务工程中抽离为独立SDK，供本地依赖复用。
 */
public class ModelScopeChatDto {

    /** /v1/chat/completions 请求体（兼容 ModelScope 的 stream 参数） */
    public static class Request {
        /** 模型名称 */
        private String model;
        /** 采样温度（可选） */
        private Double temperature;
        /** 最大生成长度（可选，字段命名兼容 OpenAI） */
        private Integer max_tokens;
        /** 消息列表 */
        private List<Message> messages;
        /** 是否流式输出 */
        private Boolean stream;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Integer getMax_tokens() { return max_tokens; }
        public void setMax_tokens(Integer max_tokens) { this.max_tokens = max_tokens; }

        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }

        public Boolean getStream() { return stream; }
        public void setStream(Boolean stream) { this.stream = stream; }
    }

    /** OpenAI 兼容的消息结构 */
    public static class Message {
        private String role;
        private String content;

        public Message() {}
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    /** /v1/chat/completions 响应体（兼容非流与流式的字段） */
    public static class Response {
        private String id;
        private String object;
        private Long created;
        private String model;
        private String system_fingerprint;
        private List<Choice> choices;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }

        public Long getCreated() { return created; }
        public void setCreated(Long created) { this.created = created; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public String getSystem_fingerprint() { return system_fingerprint; }
        public void setSystem_fingerprint(String system_fingerprint) { this.system_fingerprint = system_fingerprint; }

        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
    }

    /** 响应中的候选项（兼容 delta 与 message） */
    public static class Choice {
        private Integer index;
        private Delta delta;         // 流式 chunk 中的增量
        private String finish_reason; // 结束原因（可能为 stop）
        private Message message;     // 非流式或最终汇总的消息

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }

        public Delta getDelta() { return delta; }
        public void setDelta(Delta delta) { this.delta = delta; }

        public String getFinish_reason() { return finish_reason; }
        public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    /** 流式 chunk 的增量内容 */
    public static class Delta {
        private String role;
        private String content;
        private Object tool_calls;
        private Object function_calls;
        private String reasoning_content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Object getTool_calls() { return tool_calls; }
        public void setTool_calls(Object tool_calls) { this.tool_calls = tool_calls; }

        public Object getFunction_calls() { return function_calls; }
        public void setFunction_calls(Object function_calls) { this.function_calls = function_calls; }

        public String getReasoning_content() { return reasoning_content; }
        public void setReasoning_content(String reasoning_content) { this.reasoning_content = reasoning_content; }
    }
}
