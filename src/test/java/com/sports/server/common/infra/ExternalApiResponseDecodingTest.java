package com.sports.server.common.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.cheertalk.dto.GeminiResponse;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 외부 API 클라이언트는 WebClient.builder() 를 직접 써서 Boot 의 Jackson 설정을 받지 않고 기본 코덱으로 응답을 읽는다.
 * 실제 응답처럼 모르는 필드와 null 이 섞여 와도 읽히는지 본다.
 */
class ExternalApiResponseDecodingTest {

    private static final String GEMINI_BODY = """
            {
              "candidates": [{
                "content": {"parts": [{"text": "응원해요", "thought": null}], "role": "model"},
                "finishReason": "STOP",
                "index": 0,
                "safetyRatings": [{"category": "HARM_CATEGORY_HARASSMENT", "probability": "NEGLIGIBLE"}]
              }],
              "usageMetadata": {"promptTokenCount": 12, "candidatesTokenCount": 3, "totalTokenCount": 15},
              "modelVersion": "gemini-x",
              "responseId": null
            }
            """;

    private static final String OPENROUTER_BODY = """
            {
              "id": "gen-1",
              "provider": "p",
              "model": "m",
              "object": "chat.completion",
              "created": 1762840000,
              "choices": [{
                "logprobs": null,
                "finish_reason": "tool_calls",
                "index": 0,
                "message": {
                  "role": "assistant",
                  "content": null,
                  "refusal": null,
                  "reasoning": null,
                  "tool_calls": [{"id": "c1", "index": 0, "type": "function",
                                  "function": {"name": "f", "arguments": "{\\"a\\":1}"}}]
                }
              }],
              "usage": {"prompt_tokens": 10, "completion_tokens": 5, "total_tokens": 15, "cost": null}
            }
            """;

    private static <T> T decode(String body, Class<T> type) {
        WebClient client = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build()))
                .build();
        return client.get().uri("http://127.0.0.1:9/").retrieve().bodyToMono(type).block();
    }

    @Test
    @DisplayName("Gemini 응답에 모르는 필드와 null 이 있어도 본문 텍스트를 읽는다")
    void gemini_응답() {
        GeminiResponse response = decode(GEMINI_BODY, GeminiResponse.class);

        assertThat(response.getFirstText()).isEqualTo("응원해요");
    }

    @Test
    @DisplayName("OpenRouter 응답에 모르는 필드와 null 이 있어도 tool call 을 읽는다")
    void openrouter_응답() {
        OpenRouterChatResponse response = decode(OPENROUTER_BODY, OpenRouterChatResponse.class);

        assertThat(response.hasToolCall()).isTrue();
        assertThat(response.choices().get(0).message().tool_calls().get(0).function().arguments())
                .isEqualTo("{\"a\":1}");
        assertThat(response.getText()).isNull();
    }
}
