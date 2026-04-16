package com.sports.server.command.player.domain;

import com.sports.server.common.exception.ExceptionMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class PlayerTest {

    @Nested
    @DisplayName("선수 생성 시")
    class CreatePlayer {
        @Test
        void 이름이_null이면_예외를_던진다() {
            // given
            final String validStudentNumber = "202500000";

            // when & then
            assertThatThrownBy(() -> new Player(null, validStudentNumber, 9))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 학번_자릿수가_organization_설정과_다르면_예외를_던진다() {
            // given
            final String eightDigit = "20250000";

            // when & then
            assertThatThrownBy(() -> new Player("손흥민", eightDigit, 9))
                    .hasMessageContaining(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 9));
        }

        @Test
        void 학번에_문자가_포함되면_예외를_던진다() {
            // given
            final String invalidStudentNumber = "20250000!";

            // when & then
            assertThatThrownBy(() -> new Player("손흥민", invalidStudentNumber, 9))
                    .hasMessageContaining(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 9));
        }

        @Test
        void organization이_10자리_설정이면_10자리_학번으로_생성된다() {
            // given & when
            Player player = new Player("손흥민", "2025000001", 10);

            // then
            assertThat(player.getStudentNumber()).isEqualTo("2025000001");
        }

        @Test
        void organization이_10자리_설정일_때_9자리_학번이면_예외를_던진다() {
            // given
            final String nineDigit = "202500001";

            // when & then
            assertThatThrownBy(() -> new Player("손흥민", nineDigit, 10))
                    .hasMessageContaining(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 10));
        }
    }

    @Nested
    @DisplayName("선수 정보 수정 시")
    class UpdatePlayer {
        @Test
        void 모든_정보를_성공적으로_수정한다() {
            // given
            final Player player = new Player("박지성", "202500001", 9);

            // when
            player.update("손흥민", "202500002", 9);

            // then
            assertAll(
                    () -> assertThat(player.getName()).isEqualTo("손흥민"),
                    () -> assertThat(player.getStudentNumber()).isEqualTo("202500002")
            );
        }

        @Test
        void 유효하지_않은_학번으로_수정시_예외를_던진다() {
            // given
            final Player player = new Player("손흥민", "202500001", 9);
            final String invalidValue = "2025000";

            // when & then
            assertThatThrownBy(() -> player.update("손흥민", invalidValue, 9))
                    .hasMessageContaining(String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, 9));
        }
    }
}
