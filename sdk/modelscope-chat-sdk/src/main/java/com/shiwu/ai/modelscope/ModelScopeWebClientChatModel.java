package com.shiwu.ai.modelscope;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SDK版本：基于 WebClient 的 ChatModel 实现，面向 OpenAI 兼容的 /v1/chat/completions。
 * 本类仅负责与 HTTP 服务交互；请求/响应 DTO 在 ModelScopeChatDto 中定义。
 *
 * 与业务工程解耦后，可通过本地依赖 com.shiwu.ai:modelscope-chat-sdk 使用。
 */
@Slf4j
public class ModelScopeWebClientChatModel implements ChatModel {

    private final WebClient webClient;
    private final String model;
    private final boolean streamEnabled;
    private final Duration timeout = Duration.ofSeconds(30);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ModelScopeWebClientChatModel(WebClient webClient, String model, boolean streamEnabled) {
        this.webClient = webClient;
        this.model = model;
        this.streamEnabled = streamEnabled;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        if (CollectionUtils.isEmpty(messages)) {
            return emptyResponse();
        }

        // 构造请求体
        ModelScopeChatDto.Request req = new ModelScopeChatDto.Request();
        req.setModel(model);
        // 如需温度/长度控制，可从 Prompt 的 options 扩展映射
        req.setMessages(toDtoMessages(messages));
        req.setStream(streamEnabled);

        if (streamEnabled) {
            // 流式模式：按 SSE "data: {...}" 分片解析，聚合 delta.content
            Flux<String> flux = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(timeout);

            StringBuilder sb = new StringBuilder();
            flux.toIterable().forEach(line -> {
                String trimmed = line == null ? "" : line.trim();
                if (!trimmed.startsWith("data:")) {
                    return;
                }
                String payload = trimmed.substring(5).trim();
                if ("[DONE]".equals(payload)) {
                    return;
                }
                try {
                    ModelScopeChatDto.Response respChunk = objectMapper.readValue(payload, ModelScopeChatDto.Response.class);
                    if (respChunk.getChoices() != null && !respChunk.getChoices().isEmpty()) {
                        ModelScopeChatDto.Choice choice = respChunk.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            sb.append(choice.getDelta().getContent());
                        } else if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                            // 某些实现可能在最终块回传完整 message
                            sb.append(choice.getMessage().getContent());
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析 ModelScope 流式块失败: {}", e.getMessage());
                }
            });

            Generation generation = new Generation(new AssistantMessage(sb.toString()));
            return new ChatResponse(Collections.singletonList(generation));
        } else {
            // 非流模式：一次性 JSON 响应
            ModelScopeChatDto.Response resp = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(ModelScopeChatDto.Response.class)
                    .timeout(timeout)
                    .block();

            if (resp == null || CollectionUtils.isEmpty(resp.getChoices())) {
                log.warn("ModelScope 返回空响应或无 choices");
                return emptyResponse();
            }

            String content = "";
            ModelScopeChatDto.Choice choice = resp.getChoices().get(0);
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                content = choice.getMessage().getContent();
            } else if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                content = choice.getDelta().getContent();
            }

            Generation generation = new Generation(new AssistantMessage(safe(content)));
            return new ChatResponse(Collections.singletonList(generation));
        }
    }

    private ChatResponse emptyResponse() {
        Generation generation = new Generation(new AssistantMessage(""));
        return new ChatResponse(Collections.singletonList(generation));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    /** 将 Spring AI 的 Message 映射为 OpenAI 兼容的 DTO 消息 */
    private List<ModelScopeChatDto.Message> toDtoMessages(List<Message> messages) {
        List<ModelScopeChatDto.Message> list = new ArrayList<>();
        for (Message m : messages) {
            String role;
            if (m instanceof UserMessage) {
                role = "user";
            } else if (m instanceof AssistantMessage) {
                role = "assistant";
            } else {
                // system 等其他角色
                role = safe(m.getMessageType().getValue());
            }
            list.add(new ModelScopeChatDto.Message(role, safe(m.getText())));
        }
        return list;
    }
}
