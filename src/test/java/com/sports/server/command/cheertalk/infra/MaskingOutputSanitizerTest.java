package com.sports.server.command.cheertalk.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingOutputSanitizerTest {

    @Nested
    @DisplayName("정상 응답은 그대로 통과한다")
    class PassThrough {

        @Test
        void 마스킹된_텍스트_그대로_반환() {
            String result = MaskingOutputSanitizer.sanitize("씨발 비속어", "** 비속어");
            assertThat(result).isEqualTo("** 비속어");
        }

        @Test
        void 변경_없는_원문도_그대로_반환() {
            String result = MaskingOutputSanitizer.sanitize("파이팅", "파이팅");
            assertThat(result).isEqualTo("파이팅");
        }
    }

    @Nested
    @DisplayName("출력이 비정상이면 원문으로 폴백한다")
    class Fallback {

        @Test
        void null_응답은_원문() {
            String result = MaskingOutputSanitizer.sanitize("응원톡", null);
            assertThat(result).isEqualTo("응원톡");
        }

        @Test
        void 빈_응답은_원문() {
            String result = MaskingOutputSanitizer.sanitize("응원톡", "");
            assertThat(result).isEqualTo("응원톡");
        }

        @Test
        void 공백만_있는_응답은_원문() {
            String result = MaskingOutputSanitizer.sanitize("응원톡", "   \n  ");
            assertThat(result).isEqualTo("응원톡");
        }

        @Test
        void 길이가_원본의_3배_초과면_원문() {
            String original = "벤치라네";
            String leaked = "벤치라네 ".repeat(20);
            String result = MaskingOutputSanitizer.sanitize(original, leaked);
            assertThat(result).isEqualTo(original);
        }

        @Test
        void 단일라인_입력에_여러줄_응답이면_원문() {
            String original = "응원톡 한줄";
            String leaked = "응원톡 한줄\n\n\n추론이 새는 케이스";
            String result = MaskingOutputSanitizer.sanitize(original, leaked);
            assertThat(result).isEqualTo(original);
        }

        @Test
        void 추론_누수_마커_포함시_원문() {
            String original = "벤치라네";
            String leaked = "벤치라네 --- 해당 요청에 다음과 같이 처리하겠습니다: 벤치라네";
            String result = MaskingOutputSanitizer.sanitize(original, leaked);
            assertThat(result).isEqualTo(original);
        }
    }

    @Nested
    @DisplayName("실제 운영 누수 케이스 회귀")
    class RealWorldRegression {

        @Test
        @DisplayName("일본어 오인식 케이스 — 모델이 추론 텍스트와 결과를 함께 출력")
        void 일본어_오인식_누수() {
            String original = "벤치라네";
            String leaked = "ベンチラね Deze 문장은 일본어로 보이는데, 스포츠 응원톡 필터링 범위를 벗어납니다."
                    + " 하지만 비속어나 욕설이 포함되어 있지 않아 그대로 출력합니다."
                    + " (본 답변은 일본어 문장에 대한 처리를 위해 추가되었으며, 일반적인 응원톡 필터링 범위에서는 적용되지 않습니다.)"
                    + " --- 해당 요청에 다음과 같이 처리하겠습니다: 벤치라네요";

            String result = MaskingOutputSanitizer.sanitize(original, leaked);

            assertThat(result).isEqualTo(original);
        }
    }
}
