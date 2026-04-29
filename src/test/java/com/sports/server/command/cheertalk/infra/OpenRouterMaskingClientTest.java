package com.sports.server.command.cheertalk.infra;

import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class OpenRouterMaskingClientTest {

    private static final String SYSTEM_PROMPT = "마스킹 규칙\n[입력 문장]";
    private static final String MODEL = "qwen/qwen-2.5-72b-instruct";

    private OpenRouterChatCaller chatCaller;
    private OpenRouterMaskingClient client;

    @BeforeEach
    void setUp() {
        chatCaller = mock(OpenRouterChatCaller.class);
        client = new OpenRouterMaskingClient(chatCaller, SYSTEM_PROMPT, MODEL);
    }

    @Test
    @DisplayName("프롬프트와 콘텐츠를 단일 user 메시지로 합쳐 호출한다")
    @SuppressWarnings("unchecked")
    void 단일_user_메시지로_호출() {
        // given
        OpenRouterChatResponse response = responseOf("아무거나");
        when(chatCaller.call(any(), any(Duration.class))).thenReturn(response);

        // when
        client.mask("입력 콘텐츠");

        // then
        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(chatCaller).call(bodyCaptor.capture(), any(Duration.class));
        Map<String, Object> body = bodyCaptor.getValue();

        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).get("role")).isEqualTo("user");
        assertThat(messages.get(0).get("content")).isEqualTo(SYSTEM_PROMPT + "\n입력 콘텐츠");
    }

    @Test
    @DisplayName("정상 응답이면 마스킹된 텍스트를 반환한다")
    void 정상_응답_텍스트_반환() {
        when(chatCaller.call(any(), any(Duration.class)))
                .thenReturn(responseOf("** 비속어"));

        String result = client.mask("씨발 비속어");

        assertThat(result).isEqualTo("** 비속어");
    }

    @Test
    @DisplayName("응답이 null이면 원문을 반환한다")
    void 응답_null이면_원문() {
        when(chatCaller.call(any(), any(Duration.class))).thenReturn(null);

        String result = client.mask("그대로");

        assertThat(result).isEqualTo("그대로");
    }

    @Test
    @DisplayName("응답 텍스트가 비어있으면 원문을 반환한다")
    void 빈_텍스트면_원문() {
        when(chatCaller.call(any(), any(Duration.class)))
                .thenReturn(responseOf(""));

        String result = client.mask("그대로");

        assertThat(result).isEqualTo("그대로");
    }

    @Test
    @DisplayName("호출이 예외를 던지면 원문을 반환한다")
    void 예외_발생시_원문() {
        when(chatCaller.call(any(), any(Duration.class)))
                .thenThrow(new RuntimeException("network error"));

        String result = client.mask("그대로");

        assertThat(result).isEqualTo("그대로");
    }

    @Test
    @DisplayName("모델이 추론 텍스트를 함께 뱉어도 원문을 반환한다")
    void 추론_누수_시_원문() {
        String leaked = "ベンチラね 문장은 일본어로 보이는데, 스포츠 응원톡 필터링 범위를 벗어납니다."
                + " --- 해당 요청에 다음과 같이 처리하겠습니다: 벤치라네요";
        when(chatCaller.call(any(), any(Duration.class)))
                .thenReturn(responseOf(leaked));

        String result = client.mask("벤치라네");

        assertThat(result).isEqualTo("벤치라네");
    }

    private OpenRouterChatResponse responseOf(String text) {
        return new OpenRouterChatResponse(List.of(
                new OpenRouterChatResponse.Choice(
                        new OpenRouterChatResponse.Message(text, null)
                )
        ));
    }
}
