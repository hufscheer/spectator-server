package com.sports.server.command.player.domain;

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
        void 이름이_null이면_예외를_던진다(){
            // given
            final String validStudentNumber = "202500000";

            // when & then
            assertThatThrownBy(() -> new Player(null, validStudentNumber))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 학번이_9자리_숫자가_아니면_예외를_던진다() {
            // given
            final String invalidStudentNumber = "2025000";

            // when & then
            assertThatThrownBy(() -> new Player("손흥민", invalidStudentNumber))
                    .hasMessageContaining("학생번호는 9자리 숫자여야 합니다.");
        }

        @Test
        void 학번에_문자가_포함되면_예외를_던진다() {
            // given
            final String invalidStudentNumber = "20250000!";

            // when & then
            assertThatThrownBy(() -> new Player("손흥민", invalidStudentNumber))
                    .hasMessageContaining("학생번호는 9자리 숫자여야 합니다.");
        }
    }

    @Nested
    @DisplayName("선수 정보 수정 시")
    class UpdatePlayer {
        @Test
        void 모든_정보를_성공적으로_수정한다(){
            // given
            final Player player = new Player("박지성", "202500001");

            // when
            player.update("손흥민", "202500002");

            //then
            assertAll(
                    () -> assertThat(player.getName()).isEqualTo("손흥민"),
                    () -> assertThat(player.getStudentNumber()).isEqualTo("202500002")
            );
        }

        @Test
        void 유효하지_않은_학번으로_수정시_예외를_던진다() {
            // given
            final Player player = new Player("손흥민", "202500001");
            final String invalidValue = "2025000";

            // when & then
            assertThatThrownBy(() -> player.update("손흥민", invalidValue))
                    .hasMessageContaining("학생번호는 9자리 숫자여야 합니다.");
        }
    }

}
