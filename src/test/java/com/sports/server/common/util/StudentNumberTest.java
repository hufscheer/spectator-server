package com.sports.server.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.sports.server.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("학번 검증은")
class StudentNumberTest {

    @Nested
    @DisplayName("자리수가 정해진 조직에서")
    class FixedDigits {

        @Test
        void 그_자리수만_통과시킨다() {
            assertThatCode(() -> StudentNumber.validate("123456789", 9)).doesNotThrowAnyException();
            assertThatThrownBy(() -> StudentNumber.validate("1234567890", 9))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("9자리");
        }
    }

    @Nested
    @DisplayName("자리수를 정할 수 없는 조직에서")
    class MixedDigits {

        /**
         * 여러 학교가 한 조직으로 묶이는 대회는 학교마다 학번 자리수가 다르다.
         */
        @Test
        void 아홉자리와_열자리를_모두_받는다() {
            assertThatCode(() -> StudentNumber.validate("123456789", null)).doesNotThrowAnyException();
            assertThatCode(() -> StudentNumber.validate("1234567890", null)).doesNotThrowAnyException();
        }

        @Test
        void 범위를_벗어나면_거부한다() {
            assertThatThrownBy(() -> StudentNumber.validate("12345678", null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("9자리 또는 10자리");
            assertThatThrownBy(() -> StudentNumber.validate("12345678901", null))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void 숫자가_아니면_거부한다() {
            assertThatThrownBy(() -> StudentNumber.validate("12345678a", null))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        void isInvalid_도_널을_받는다() {
            assertThat(StudentNumber.isInvalid("123456789", null)).isFalse();
            assertThat(StudentNumber.isInvalid("12345678", null)).isTrue();
        }
    }
}
