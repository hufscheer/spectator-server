package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuarterResolverTest {

    @ParameterizedTest
    @CsvSource({
            "FIRST_HALF,     com.sports.server.command.league.domain.SoccerQuarter",
            "SECOND_HALF,    com.sports.server.command.league.domain.SoccerQuarter",
            "FIRST_QUARTER,  com.sports.server.command.league.domain.BasketballQuarter",
            "SECOND_QUARTER, com.sports.server.command.league.domain.BasketballQuarter"
    })
    void 종목별_쿼터를_올바른_구현체로_resolve한다(String value, String expectedClassName) throws ClassNotFoundException {
        Quarter quarter = QuarterResolver.resolve(value);
        assertThat(quarter.getClass().getName()).isEqualTo(expectedClassName);
    }

    @Test
    void 축구_쿼터를_이름으로_resolve한다() {
        assertThat(QuarterResolver.resolve("FIRST_HALF")).isEqualTo(SoccerQuarter.FIRST_HALF);
        assertThat(QuarterResolver.resolve("SECOND_HALF")).isEqualTo(SoccerQuarter.SECOND_HALF);
        assertThat(QuarterResolver.resolve("PENALTY_SHOOTOUT")).isEqualTo(SoccerQuarter.PENALTY_SHOOTOUT);
    }

    @Test
    void 농구_쿼터를_이름으로_resolve한다() {
        assertThat(QuarterResolver.resolve("FIRST_QUARTER")).isEqualTo(BasketballQuarter.FIRST_QUARTER);
        assertThat(QuarterResolver.resolve("FOURTH_QUARTER")).isEqualTo(BasketballQuarter.FOURTH_QUARTER);
        assertThat(QuarterResolver.resolve("OVERTIME")).isEqualTo(BasketballQuarter.OVERTIME);
    }

    @Test
    void PRE_GAME은_CommonQuarter로_resolve된다() {
        Quarter result = QuarterResolver.resolve("PRE_GAME");
        assertThat(result).isInstanceOf(CommonQuarter.class);
        assertThat(result.getOrder()).isEqualTo(0);
    }

    @Test
    void 한글_displayName으로_resolve한다() {
        assertThat(QuarterResolver.resolve("전반전")).isEqualTo(SoccerQuarter.FIRST_HALF);
        assertThat(QuarterResolver.resolve("1쿼터")).isEqualTo(BasketballQuarter.FIRST_QUARTER);
    }

    @Test
    void 존재하지_않는_값이면_예외가_발생한다() {
        assertThatThrownBy(() -> QuarterResolver.resolve("INVALID"))
                .isInstanceOf(BadRequestException.class);
    }
}
