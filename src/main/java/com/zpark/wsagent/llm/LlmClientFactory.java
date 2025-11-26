package com.zpark.wsagent.llm;

import com.zpark.wsagent.llm.LlmClient.ModelType;
import com.zpark.wsagent.llm.impl.DashScopeLlmClient;
import com.zpark.wsagent.llm.impl.ModelScopeLlmClient;
import com.zpark.wsagent.llm.impl.OllamaLlmClient;

/**
 * LLM 客户端工厂（简单注册表）
 *
 * - 根据 Agent 的模型类型(ModelType)返回对应的 LlmClient 实现
 * - 当前实现：支持 OLLAMA、QWEN/DASH_SCOPE、MODELSCOPE；其它类型暂回退 OLLAMA
 */
public class LlmClientFactory {

    /**
     * 获取对应模型类型的客户端实现
     * @param type 模型类型
     * @return LlmClient 实例
     */
    public static LlmClient get(ModelType type) {
        if (type == null) {
            // 默认走本地 OLLAMA
            return new OllamaLlmClient();
        }
        switch (type) {
            case OLLAMA:
                return new OllamaLlmClient();
            case QWEN:
            case DASH_SCOPE:
                // Qwen 与 DashScope 统一使用 DashScope 客户端
                return new DashScopeLlmClient();
            case MODELSCOPE:
                return new ModelScopeLlmClient();
            // 以下类型暂未实现，均回退到 OLLAMA
            case GPT_4:
            case CLAUDE:
            default:
                return new OllamaLlmClient();
        }
    }
}
