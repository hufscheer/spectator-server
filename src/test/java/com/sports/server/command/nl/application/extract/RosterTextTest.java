package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.nl.dto.NlMessageLimit;
import org.junit.jupiter.api.Test;

class RosterTextTest {

    @Test
    void 빈_줄은_버린다() {
        RosterText text = RosterText.ofText("a\n\n  \nb\r\nc\n");

        assertThat(text.text()).isEqualTo("a\nb\nc");
        assertThat(text.truncated()).isFalse();
    }

    @Test
    void 정확히_오백줄이면_자르지_않고_오백일줄부터_truncated() {
        RosterText exact = new RosterText();
        for (int i = 0; i < 500; i++) {
            exact.addLine("x" + i);
        }
        assertThat(exact.truncated()).isFalse();

        exact.addLine("one more");
        assertThat(exact.truncated()).isTrue();
        assertThat(exact.text().split("\n")).hasSize(500);
    }

    @Test
    void 글자수가_5000을_넘기_전의_줄까지만_담는다() {
        RosterText text = new RosterText();
        String line = "x".repeat(999);
        for (int i = 0; i < 6; i++) {
            text.addLine(line);
        }

        assertThat(text.text().length()).isEqualTo(5 * 999 + 4);
        assertThat(text.text().length()).isLessThanOrEqualTo(NlMessageLimit.MAX_LENGTH);
        assertThat(text.truncated()).isTrue();
    }

    @Test
    void 정확히_5000자까지는_자르지_않는다() {
        RosterText text = new RosterText();
        text.addLine("x".repeat(2500));
        text.addLine("y".repeat(2499));

        assertThat(text.text().length()).isEqualTo(5000);
        assertThat(text.truncated()).isFalse();
    }

    @Test
    void 행은_탭으로_잇고_뒤쪽_빈칸과_칸_안의_탭줄바꿈을_정리한다() {
        RosterText text = new RosterText();
        text.addRow(java.util.Arrays.asList("홍길동", " 2023\t1 ", null, ""));

        assertThat(text.text()).isEqualTo("홍길동\t2023 1");
    }
}
