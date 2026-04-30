package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.application.MaskingClient;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "masking.provider", havingValue = "openrouter")
public class OpenRouterMaskingClient implements MaskingClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final OpenRouterChatCaller chatCaller;
    private final MaskingOutputSanitizer sanitizer;
    private final String systemPrompt;
    private final String model;

    public OpenRouterMaskingClient(
            OpenRouterChatCaller chatCaller,
            MaskingOutputSanitizer sanitizer,
            @Value("${openrouter.api.masking-prompt:${gemini.api.prompt}}") String systemPrompt,
            @Value("${openrouter.api.masking-model:${openrouter.api.model:qwen/qwen-2.5-72b-instruct}}") String model
    ) {
        this.chatCaller = chatCaller;
        this.sanitizer = sanitizer;
        this.systemPrompt = systemPrompt;
        this.model = model;
    }

    @Override
    public String mask(String content) {
        String fullInput = systemPrompt + "\n" + content;
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", fullInput)
                )
        );

        try {
            OpenRouterChatResponse response = chatCaller.call(body, REQUEST_TIMEOUT);
            if (response == null) {
                return content;
            }
            return sanitizer.sanitize(content, response.getText());
        } catch (Exception e) {
            log.error("OpenRouter masking failed: {}", e.getMessage());
            return content;
        }
    }
}
