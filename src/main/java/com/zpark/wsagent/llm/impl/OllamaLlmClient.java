package com.zpark.wsagent.llm.impl;

import com.zpark.wsagent.llm.LlmClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * Ollama 模型适配器实现
 *
 * - 使用 Spring AI 的 OllamaChatModel
 * - 通过 LlmClient 接口统一生成回复
 */
public class OllamaLlmClient implements LlmClient {

    @Override
    public String generate(LlmRequest request) {
        // 构造 prompt
        String prompt = buildPrompt(request.getSystemMemory(), request.getDynamicMemory(), request.getUserMessage());

        // 构建 Ollama API 与 ChatModel
        OllamaApi api = OllamaApi.builder().build();

        double temperature = request.temperature(0.7);
        String modelName = (request.getModelName() == null || request.getModelName().isEmpty())
                ? "qwen3:8b"
                : request.getModelName();

        OllamaChatModel model = OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model(modelName)
                                .temperature(temperature)
                                .build()
                )
                .build();

        // 直接使用文本调用
        return model.call(prompt);
    }
}
