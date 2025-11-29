package com.zpark.wsagent.chatModel.impl;

import java.util.List;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.shiwu.ai.modelscope.ModelScopeWebClientChatModel;
import com.zpark.wsagent.chatModel.AgentProvider;

/**
 * ModelScope 构建器：
 * - 仅负责暴露 ChatClient.Builder
 * - 通过 WebClient + ModelScopeWebClientChatModel 调用兼容 OpenAI 的 /v1/chat/completions
 *
 * 使用方式（application.properties）：
 *   ai.vendor=modelscope
 *   spring.ai.modelscope.api-key=${MODELSCOPE_API_KEY}
 *   spring.ai.modelscope.base-url=https://你的-ModelScope-兼容服务域名
 *   spring.ai.modelscope.chat.options.model=你的模型名
 *
 * 说明：
 * - 具体的 HTTP 请求/响应数据结构见 ModelScopeChatDto
 * - 具体的 WebClient 调用逻辑见 ModelScopeWebClientChatModel
 */
@Configuration
public class modelScopeAi implements AgentProvider {

    @Value("${spring.ai.modelscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.modelscope.base-url}")
    private String baseUrl;

    @Value("${spring.ai.modelscope.stream:false}")
    private boolean stream;
    /**
     * 暴露 ChatClient.Builder，供 AiBuilderConfig 注入使用
     */
    public ReactAgent getAgent(String model,String prompt,List<ToolCallback> tool) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        ChatModel chatModel = new ModelScopeWebClientChatModel(webClient, model, stream);
        ReactAgent agent = ReactAgent.builder()
                .name("modelscope-agent")
                .model(chatModel)
                .instruction(prompt)
                .build();
        return agent;
    }

    @Override
    public String getVendor() {
        return "modelscope";
    }
}
