package com.sports.server.command.nl.infra;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_AI_UNAVAILABLE;

import com.sports.server.common.exception.CustomException;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class NlTranscriptionClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final String PDF_MIME_TYPE = "application/pdf";

    // OpenRouterChatCaller 는 openrouter 제공자가 켜졌을 때만 빈이 있다. 없으면 호출 시점에 503 으로 응답한다.
    private final ObjectProvider<OpenRouterChatCaller> chatCaller;
    private final String model;
    private final String prompt;
    private final String reasoningEffort;
    private final int maxTokens;

    public NlTranscriptionClient(
            ObjectProvider<OpenRouterChatCaller> chatCaller,
            @Value("${nl.extract.model:google/gemini-3.8-flash}") String model,
            @Value("${nl.extract.prompt}") String prompt,
            @Value("${nl.extract.reasoning-effort:minimal}") String reasoningEffort,
            @Value("${nl.extract.max-tokens:4000}") int maxTokens
    ) {
        this.chatCaller = chatCaller;
        this.model = model;
        this.prompt = prompt;
        this.reasoningEffort = reasoningEffort;
        this.maxTokens = maxTokens;
    }

    public String transcribe(byte[] data, String mimeType) {
        OpenRouterChatCaller caller = chatCaller.getIfAvailable();
        if (caller == null) {
            log.error("OpenRouter caller is not configured for nl extract");
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, EXTRACT_AI_UNAVAILABLE);
        }
        OpenRouterChatResponse response;
        try {
            response = caller.call(buildRequestBody(data, mimeType), REQUEST_TIMEOUT);
        } catch (WebClientResponseException | IllegalStateException e) {
            log.error("OpenRouter extract call failed after retries: {}", e.getClass().getSimpleName());
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, EXTRACT_AI_UNAVAILABLE);
        }
        return clean(response == null ? null : response.getText());
    }

    private Map<String, Object> buildRequestBody(byte[] data, String mimeType) {
        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(data);
        Map<String, Object> filePart = isPdf(mimeType)
                ? Map.of("type", "file", "file", Map.of("filename", "roster.pdf", "file_data", dataUrl))
                : Map.of("type", "image_url", "image_url", Map.of("url", dataUrl));
        return Map.of(
                "model", model,
                "reasoning", Map.of("effort", reasoningEffort),
                "max_tokens", maxTokens,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(Map.of("type", "text", "text", prompt), filePart)
                ))
        );
    }

    private static boolean isPdf(String mimeType) {
        return PDF_MIME_TYPE.equals(mimeType);
    }

    private static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.strip();
        if (text.startsWith("```") && text.endsWith("```") && text.length() >= 6) {
            String inner = text.substring(3, text.length() - 3);
            int firstNewline = inner.indexOf('\n');
            // 여는 펜스 줄의 언어 표시(```text)는 본문이 아니다
            inner = firstNewline < 0 ? inner : inner.substring(firstNewline + 1);
            text = inner.strip();
        }
        return text;
    }
}
