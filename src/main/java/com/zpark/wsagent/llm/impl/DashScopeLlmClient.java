package com.zpark.wsagent.llm.impl;

import com.zpark.wsagent.llm.LlmClient;
import com.zpark.wsagent.llm.LlmClient.LlmRequest;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * DashScope(Qwen) 模型适配器实现
 *
 * - 基于 Alibaba Cloud AI 的 DashScopeChatModel
 * - 支持通过 LlmRequest 传入系统记忆、动态记忆与用户消息
 */
public class DashScopeLlmClient implements LlmClient {

    @Override
    public String generate(LlmRequest request) {
        String systemMemory = safe(request.getSystemMemory());
        List<String> dynamicMem = request.getDynamicMemory() == null ? List.of() : request.getDynamicMemory();
        String userMessage = safe(request.getUserMessage());

        // 这里不直接设置选项，依赖全局配置（application.properties）
        // 如需按请求设置模型名/温度，可扩展为从 extraParams 读取并构造自定义 DashScopeApi
        DashScopeChatModel model = DashScopeChatModel.builder()
                .build();

        // 组装对话消息：system + dynamic memories + user
        List<Message> messages = new ArrayList<>();
        if (!systemMemory.isEmpty()) {
            messages.add(new SystemMessage(systemMemory));
        }
        for (String mem : dynamicMem) {
            messages.add(new AssistantMessage(safe(mem)));
        }
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);
        return model.call(prompt).getResult().getOutput().getText();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
