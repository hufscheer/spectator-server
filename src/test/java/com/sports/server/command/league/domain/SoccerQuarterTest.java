package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SoccerQuarterTest {

    @ParameterizedTest
    @CsvSource({
            "FIRST_HALF, FIRST_HALF",
            "SECOND_HALF,SECOND_HALF",
            "EXTRA_TIME, EXTRA_TIME",
            "PENALTY_SHOOTOUT, PENALTY_SHOOTOUT"
    })
    void enum_이름으로_resolve한다(String value, SoccerQuarter expected) {
        assertThat(SoccerQuarter.resolve(value)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "전반전, FIRST_HALF",
            "후반전, SECOND_HALF",
            "연장전, EXTRA_TIME",
            "승부차기, PENALTY_SHOOTOUT"
    })
    void 한글_displayName으로_resolve한다(String value, SoccerQuarter expected) {
        assertThat(SoccerQuarter.resolve(value)).isEqualTo(expected);
    }

    @Test
    void 존재하지_않는_값이면_예외가_발생한다() {
        assertThatThrownBy(() -> SoccerQuarter.resolve("INVALID"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void tryResolve는_존재하는_값이면_Optional을_반환한다() {
        assertThat(SoccerQuarter.tryResolve("FIRST_HALF")).contains(SoccerQuarter.FIRST_HALF);
    }

    @Test
    void tryResolve는_존재하지_않는_값이면_Optional_empty를_반환한다() {
        assertThat(SoccerQuarter.tryResolve("INVALID")).isEmpty();
    }

    @Test
    void firstQuarter는_FIRST_HALF를_반환한다() {
        for (SoccerQuarter quarter : SoccerQuarter.values()) {
            assertThat(quarter.firstQuarter()).isEqualTo(SoccerQuarter.FIRST_HALF);
        }
    }

    @Test
    void 쿼터_순서가_오름차순이다() {
        SoccerQuarter[] quarters = SoccerQuarter.values();
        for (int i = 0; i < quarters.length - 1; i++) {
            assertThat(quarters[i].getOrder()).isLessThan(quarters[i + 1].getOrder());
        }
    }
}
