package com.zpark.wsagent.chatModel;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;

/**
 * 统一的Agent提供器接口：
 * - getAgent：根据模型名构建并返回对应的 ReactAgent
 * - getVendor：返回供应商标识（如 "modelscope"、"dashscope"、"ollama"）
 */
public interface AgentProvider {

    /**
     * 构建并返回指定模型名的 ReactAgent
     * @param model 模型名称
     * @return ReactAgent 实例
     */
    ReactAgent getAgent(String model,String prompt,List<ToolCallback> tool);

    /**
     * 返回供应商标识，用于工厂按供应商路由
     * @return 供应商标识字符串
     */
    String getVendor();
}
