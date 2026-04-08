package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BasketballQuarterTest {

    @ParameterizedTest
    @CsvSource({
            "PRE_GAME,       PRE_GAME",
            "FIRST_QUARTER,  FIRST_QUARTER",
            "SECOND_QUARTER, SECOND_QUARTER",
            "THIRD_QUARTER,  THIRD_QUARTER",
            "FOURTH_QUARTER, FOURTH_QUARTER",
            "OVERTIME,       OVERTIME",
            "POST_GAME,      POST_GAME"
    })
    void enum_이름으로_resolve한다(String value, BasketballQuarter expected) {
        assertThat(BasketballQuarter.resolve(value)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "경기전, PRE_GAME",
            "1쿼터,  FIRST_QUARTER",
            "2쿼터,  SECOND_QUARTER",
            "3쿼터,  THIRD_QUARTER",
            "4쿼터,  FOURTH_QUARTER",
            "연장전, OVERTIME",
            "경기후, POST_GAME"
    })
    void 한글_displayName으로_resolve한다(String value, BasketballQuarter expected) {
        assertThat(BasketballQuarter.resolve(value)).isEqualTo(expected);
    }

    @Test
    void 존재하지_않는_값이면_예외가_발생한다() {
        assertThatThrownBy(() -> BasketballQuarter.resolve("FIRST_HALF"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void tryResolve는_존재하는_값이면_Optional을_반환한다() {
        assertThat(BasketballQuarter.tryResolve("FIRST_QUARTER")).contains(BasketballQuarter.FIRST_QUARTER);
    }

    @Test
    void tryResolve는_존재하지_않는_값이면_Optional_empty를_반환한다() {
        assertThat(BasketballQuarter.tryResolve("FIRST_HALF")).isEmpty();
    }

    @Test
    void firstQuarter는_FIRST_QUARTER를_반환한다() {
        for (BasketballQuarter quarter : BasketballQuarter.values()) {
            assertThat(quarter.firstQuarter()).isEqualTo(BasketballQuarter.FIRST_QUARTER);
        }
    }

    @Test
    void PRE_GAME과_POST_GAME이_SoccerQuarter_없이_독립적으로_존재한다() {
        assertThat(BasketballQuarter.PRE_GAME).isNotNull();
        assertThat(BasketballQuarter.POST_GAME).isNotNull();
        assertThat(BasketballQuarter.PRE_GAME.getOrder()).isEqualTo(0);
    }

    @Test
    void 쿼터_순서가_오름차순이다() {
        BasketballQuarter[] quarters = BasketballQuarter.values();
        for (int i = 0; i < quarters.length - 1; i++) {
            assertThat(quarters[i].getOrder()).isLessThan(quarters[i + 1].getOrder());
        }
    }
}
