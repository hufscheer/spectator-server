package com.sports.server.command.cheertalk.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingPreFilterTest {

    private final MaskingPreFilter filter = new MaskingPreFilter(
            List.of("가즈아🔥", "나이스👍", "까비😭️"),
            List.of("ㅍㅇㅌ", "ㅎㅇㅌ", "ㅎㅇ", "ㄱㄱ", "ㄱㅅ", "ㅊㅋ", "ㄷㄷ", "ㄹㅇ", "ㅇㅈ", "ㄴㄴ", "ㅇㅇ")
    );

    @Test
    @DisplayName("null이거나 공백만 있으면 LLM을 스킵한다")
    void null_또는_공백_스킵() {
        assertThat(filter.canSkip(null)).isTrue();
        assertThat(filter.canSkip("")).isTrue();
        assertThat(filter.canSkip("   ")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"가즈아🔥", "나이스👍", "까비😭️"})
    @DisplayName("프론트 추천 문구는 정확 매치 시 LLM을 스킵한다")
    void 추천_문구_정확_매치_스킵(String message) {
        assertThat(filter.canSkip(message)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ㅍㅇㅌ", "ㅎㅇㅌ", "ㅎㅇ", "ㄱㄱ", "ㄱㅅ", "ㅊㅋ", "ㄷㄷ", "ㄹㅇ", "ㅇㅈ", "ㄴㄴ", "ㅇㅇ"})
    @DisplayName("응원/긍정 초성은 LLM을 스킵한다")
    void 긍정_초성_스킵(String consonant) {
        assertThat(filter.canSkip(consonant)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Yes!!", "GG", "1234", "🔥🔥🔥", "👍", "wow", "!!!"})
    @DisplayName("한글이 전혀 없는 메시지는 LLM을 스킵한다")
    void 한글_없으면_스킵(String message) {
        assertThat(filter.canSkip(message)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ㅅㅂ", "ㅂㅅ", "ㄱㅅㄲ", "ㅈㄴ", "ㅄ", "씨발", "개새끼", "응원합니다", "가즈아!", "가즈아"})
    @DisplayName("욕설 의심 자모와 한글 음절을 포함한 메시지는 LLM에 위임한다")
    void 욕설_의심은_LLM_위임(String message) {
        assertThat(filter.canSkip(message)).isFalse();
    }

    @Test
    @DisplayName("앞뒤 공백은 무시하고 정확 매치를 판단한다")
    void 공백_무시() {
        assertThat(filter.canSkip("  가즈아🔥  ")).isTrue();
        assertThat(filter.canSkip(" ㄱㄱ ")).isTrue();
    }
}
