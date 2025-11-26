package com.zpark.wsagent.llm.impl;

import com.zpark.wsagent.llm.LlmClient;
import com.zpark.wsagent.llm.LlmClient.LlmRequest;
import com.shiwu.ai.modelscope.ModelScopeWebClientChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * ModelScope 模型适配器实现
 *
 * - 使用本地SDK ModelScopeWebClientChatModel，通过 WebClient 访问 /v1/chat/completions 兼容接口
 * - 支持通过 LlmRequest 传入系统记忆、动态记忆与用户消息
 */
public class ModelScopeLlmClient implements LlmClient {

    @Override
    public String generate(LlmRequest request) {
        String systemMemory = safe(request.getSystemMemory());
        List<String> dynamicMem = request.getDynamicMemory() == null ? List.of() : request.getDynamicMemory();
        String userMessage = safe(request.getUserMessage());

        java.util.Map<String, Object> extra = request.getExtraParams();
        String baseUrl = (extra != null && extra.get("modelscopeBaseUrl") instanceof String)
                ? (String) extra.get("modelscopeBaseUrl")
                : "https://api.modelscope.cn/v1";
        String apiKey = (extra != null && extra.get("modelscopeApiKey") instanceof String)
                ? (String) extra.get("modelscopeApiKey")
                : "";

        String modelName = (request.getModelName() == null || request.getModelName().isEmpty())
                ? "qwen-turbo"
                : request.getModelName();

        // 构建 WebClient，附带鉴权头
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", apiKey.isBlank() ? "" : "Bearer " + apiKey)
                .build();

        // 构建 ChatModel（非流式）
        ModelScopeWebClientChatModel chatModel = new ModelScopeWebClientChatModel(webClient, modelName, false);

        // 组装消息：system + dynamic memories(以 assistant 复述) + user
        List<Message> messages = new ArrayList<>();
        if (!systemMemory.isEmpty()) {
            messages.add(new SystemMessage(systemMemory));
        }
        for (String mem : dynamicMem) {
            messages.add(new AssistantMessage(safe(mem)));
        }
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);
        ChatResponse resp = chatModel.call(prompt);
        return resp.getResult().getOutput().getText();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
