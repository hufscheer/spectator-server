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
import java.util.Map;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
                "골 프롬프트 {team_name} {scorer_name}",
                "자책골 프롬프트 {team_name}"
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
        @DisplayName("OWN_GOAL 트리거는 선수명 없이도 정상 응답을 반환한다")
        void OWN_GOAL_정상_응답() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("어...?"));

            String result = generator.generate(AiSeedTriggerType.OWN_GOAL, "경영", null);

            assertThat(result).isEqualTo("어...?");
        }

        @Test
        @DisplayName("길이를 넘기면 잘라내지 않고 버려서 fallback 을 쓴다")
        void 길이_초과는_버린다() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 후반전도 파이팅하자 이번엔 진짜 간다"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            // 잘라서 "경영 후반전도 파이팅하자 이번" 을 내보내면 문장이 중간에서 끊긴다
            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("첫 응답을 버리면 한 번 더 물어보고 그 답을 쓴다")
        void 버려지면_한_번_더() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 후반전도 파이팅하자 이번엔 진짜 간다"), responseOf("경영 간다"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 간다");
            verify(chatCaller, times(2)).call(any(), any(Duration.class));
        }

        @Test
        @DisplayName("여러 줄을 내놓으면 첫 줄만 쓴다")
        void 여러_줄은_첫_줄만() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("경영 가자\n오늘 기대됨\n이건 못참지"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 가자");
        }

        @Test
        @DisplayName("앞에 붙은 기호를 떼어 낸다")
        void 앞_기호_제거() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenReturn(responseOf("- 경영 간다"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            assertThat(result).isEqualTo("경영 간다");
        }
    }

    @Nested
    @DisplayName("긴 팀 이름")
    class LongTeamName {

        private static final String LONG_TEAM = "서울시립대학교 남자축구 대표팀";

        @Test
        @DisplayName("팀 이름을 쓰지 말라는 조건을 프롬프트에 덧붙인다")
        void 프롬프트에_금지_조건() {
            when(chatCaller.call(any(), any(Duration.class))).thenReturn(responseOf("오늘 가보자"));

            generator.generate(AiSeedTriggerType.SCHEDULED, LONG_TEAM, null);

            ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
            verify(chatCaller).call(body.capture(), any(Duration.class));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) body.getValue().get("messages");
            assertThat(messages.get(0).get("content")).contains("팀 이름을 쓰지 말고");
        }

        @Test
        @DisplayName("fallback 도 긴 팀 이름을 쓰지 않는다")
        void fallback_은_팀명을_피한다() {
            when(chatCaller.call(any(), any(Duration.class))).thenReturn(responseOf("ㅋㅋ"));

            String result = generator.generate(AiSeedTriggerType.SCHEDULED, LONG_TEAM, null);

            // "서울시립대학교 남자축구 대표팀 가자" 는 사람이 치는 말이 아니다
            assertThat(result).doesNotContain(LONG_TEAM).isEqualTo("오늘 가보자");
        }

        @Test
        @DisplayName("짧은 팀 이름에는 금지 조건을 붙이지 않는다")
        void 짧은_팀명은_그대로() {
            when(chatCaller.call(any(), any(Duration.class))).thenReturn(responseOf("경영 가자"));

            generator.generate(AiSeedTriggerType.SCHEDULED, "경영", null);

            ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
            verify(chatCaller).call(body.capture(), any(Duration.class));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) body.getValue().get("messages");
            assertThat(messages.get(0).get("content")).doesNotContain("팀 이름을 쓰지 말고");
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
        @DisplayName("LLM 호출 실패 시 OWN_GOAL fallback은 선수명을 포함하지 않는다")
        void LLM_실패_OWN_GOAL_fallback() {
            when(chatCaller.call(any(), any(Duration.class)))
                    .thenThrow(new RuntimeException("network error"));

            String result = generator.generate(AiSeedTriggerType.OWN_GOAL, "경영", null);

            assertThat(result).isEqualTo("이건 예상 못했네");
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