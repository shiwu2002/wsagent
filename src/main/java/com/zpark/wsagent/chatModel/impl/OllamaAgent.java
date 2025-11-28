package com.zpark.wsagent.chatModel.impl;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.chatModel.AgentProvider;

import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaAgent implements AgentProvider {


    @Override
    public ReactAgent getAgent(String model,String prompt) { // 统一接口方法：根据模型名返回 ReactAgent
        // 构建Ollama API客户端
        OllamaApi builder = OllamaApi.builder() // 通过静态 builder() 方法获取 OllamaApi 的构建器
                .build(); // 调用 build() 生成 OllamaApi 客户端实例（使用默认配置）

        // 创建Ollama聊天模型并配置默认选项
        OllamaChatModel ollamaChatModel = OllamaChatModel.builder() // 获取 OllamaChatModel 的构建器，用于创建聊天模型
                .ollamaApi(builder) // 注入上面创建的 OllamaApi 客户端，使模型通过该客户端与 Ollama 服务通信
                .defaultOptions( // 设置模型的默认推理选项
                        OllamaChatOptions.builder() // 获取 OllamaChatOptions 的构建器
                                .model(model) // 指定默认使用的模型名称为 "qwen3:8b"
                                .temperature(0.8) // 设置采样温度为 0.8（更具创造性，输出多样性更高）
                                .build() // 构建默认选项对象
                )
                .build(); // 构建出配置完成的 OllamaChatModel 实例

        ReactAgent agent = ReactAgent.builder() // 创建一个 ReactAgent.Builder 对象，用于构建一个 ReactAgent 实例
                .name(model) // 设置代理名称为 "ollama-agent"
                .model(ollamaChatModel) // 将之前创建的 OllamaChatModel 模型注入到代理中
                .instruction(prompt)
                .build(); // 构建出配置完成的 ReactAgent 实例并返回
        return agent;

    }

    @Override
    public String getVendor() {
        return "ollama";
    }
}
