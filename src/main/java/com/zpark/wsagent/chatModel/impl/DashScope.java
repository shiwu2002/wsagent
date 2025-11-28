package com.zpark.wsagent.chatModel.impl;



import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.chatModel.AgentProvider;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashScope implements AgentProvider {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Value("${spring.ai.dashscope.apiUrl}")
    private String apiUrl;

    @Override
    public ReactAgent getAgent(String modelName,String prompt) {
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .baseUrl(apiUrl)
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name(modelName)
                .model(chatModel)
                .instruction(prompt)
                .build();
        return agent;
    }

    @Override
    public String getVendor() {
        return "dashscope";
    }
}
