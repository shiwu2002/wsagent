package com.zpark.wsagent.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zpark.wsagent.chatModel.AgentProvider;

/**
 * Agent 工厂：
 * - 基于供应商标识（vendor）路由到对应的 AgentProvider
 * - 统一对外暴露 getAgent(vendor, model) 方法
 *
 * 用法示例：
 *   ReactAgent agent = agentFactory.getAgent("modelscope", "qwen2.5");
 *   ReactAgent agent = agentFactory.getAgent("dashscope", "qwen-turbo");
 *   ReactAgent agent = agentFactory.getAgent("ollama", "llama3");
 */
@Component
public class AgentFactory {

    private final Map<String, AgentProvider> providerRegistry = new HashMap<>();

    /**
     * 通过 Spring 注入所有实现了 AgentProvider 的 Bean，并构建 vendor -> provider 的路由表
     */
    public AgentFactory(List<AgentProvider> providers) {
        for (AgentProvider provider : providers) {
            providerRegistry.put(provider.getVendor().toLowerCase(), provider);
        }
    }

    /**
     * 根据供应商(vendor)与模型名(model)返回 ReactAgent
     *
     * @param vendor 供应商标识（如 "modelscope"、"dashscope"、"ollama"）
     * @param model  模型名称
     * @return ReactAgent
     * @throws IllegalArgumentException 当 vendor 未注册时抛出
     */
    public ReactAgent getAgent(String vendor, String model,String prompt) {
        if (vendor == null || vendor.isEmpty()) {
            throw new IllegalArgumentException("vendor 不能为空");
        }
        AgentProvider provider = providerRegistry.get(vendor.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("未找到对应的 AgentProvider，vendor=" + vendor + "。已注册的供应商：" + providerRegistry.keySet());
        }
        return provider.getAgent(model,prompt);
    }

    /**
     * 返回已注册的供应商列表
     */
    public List<String> getVendors() {
        return providerRegistry.keySet().stream().sorted().toList();
    }
}
