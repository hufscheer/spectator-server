package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSeedMessageGeneratorTest {

    private OpenRouterChatCaller chatCaller;
    private AiSeedMessageGenerator generator;

    @BeforeEach
    void setUp() {
        chatCaller = mock(OpenRouterChatCaller.class);
        generator = new AiSeedMessageGenerator(
                chatCaller,
                "qwen/qwen-2.5-72b-instruct",
                15,
                "ㅋㅋ,ㅋㅋㅋ,ㅋㅋㅋㅋ,ㄷㄷ,ㄷㄷㄷ,와,ㅎㅎ,ㅎㅎㅎ,ㄹㅇ",
                "경기 시작 전 프롬프트 {team_name}",
                "후반전 프롬프트 {team_name}",
                "골 프롬프트 {team_name} {scorer_name}"
        );
    }

    @Nested
    @DisplayName("정상 LLM 응답")
    class NormalResponse {

        @Test
        @DisplayName("정상 응답이면 그대로 반환한다")
        void 정상_응답_반환() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 가자"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("따옴표로 감싸진 응답은 따옴표를 제거한다")
        void 따옴표_제거() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("\"경영 간다\""));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 간다");
        }

        @Test
        @DisplayName("15자 초과 시 잘라낸다")
        void 길이_초과_잘라냄() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 후반전도 파이팅하자 이번엔 진짜 간다"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result.length()).isLessThanOrEqualTo(15);
        }
    }

    @Nested
    @DisplayName("단독 리액션 필터")
    class SoloReactionFilter {

        @Test
        @DisplayName("단독 리액션이면 fallback을 반환한다")
        void 단독_리액션_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("ㅋㅋ"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("문구 뒤 리액션은 허용한다")
        void 문구_뒤_리액션_허용() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 간다 ㅋㅋ"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 간다 ㅋㅋ");
        }
    }

    @Nested
    @DisplayName("fallback")
    class Fallback {

        @Test
        @DisplayName("LLM 호출 실패 시 SCHEDULED fallback을 반환한다")
        void LLM_실패_SCHEDULED_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenThrow(new RuntimeException("network error"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("LLM 호출 실패 시 GOAL fallback에 득점자명이 포함된다")
        void LLM_실패_GOAL_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenThrow(new RuntimeException("network error"));

            String result = generator.generate(AiSeedTriggerType.GOAL, "경영", "민준");

            assertThat(result).isEqualTo("민준 좋았다");
        }

        @Test
        @DisplayName("응답이 null이면 fallback을 반환한다")
        void 응답_null_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(null);

            String result = generator.generate(AiSeedTriggerType.SECOND_HALF_START, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("응답이 빈 문자열이면 fallback을 반환한다")
        void 빈_응답_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf(""));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }
    }

    private OpenRouterChatResponse responseOf(String text) {
        return new OpenRouterChatResponse(List.of(
                new OpenRouterChatResponse.Choice(
                        new OpenRouterChatResponse.Message(text, null)
                )
        ));
    }
}