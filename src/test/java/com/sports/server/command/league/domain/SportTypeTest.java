package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class SportTypeTest {

    @Test
    void 축구_종목은_축구_쿼터로_resolve한다() {
        assertThat(SportType.SOCCER.resolveQuarter("FIRST_HALF")).isEqualTo(SoccerQuarter.FIRST_HALF);
        assertThat(SportType.SOCCER.resolveQuarter("PENALTY_SHOOTOUT")).isEqualTo(SoccerQuarter.PENALTY_SHOOTOUT);
    }

    @Test
    void 농구_종목은_농구_쿼터로_resolve한다() {
        assertThat(SportType.BASKETBALL.resolveQuarter("FIRST_QUARTER")).isEqualTo(BasketballQuarter.FIRST_QUARTER);
        assertThat(SportType.BASKETBALL.resolveQuarter("OVERTIME")).isEqualTo(BasketballQuarter.OVERTIME);
    }

    @Test
    void 축구에_농구_쿼터를_전달하면_예외가_발생한다() {
        assertThatThrownBy(() -> SportType.SOCCER.resolveQuarter("FIRST_QUARTER"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 농구에_축구_전용_쿼터를_전달하면_예외가_발생한다() {
        assertThatThrownBy(() -> SportType.BASKETBALL.resolveQuarter("FIRST_HALF"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 축구의_firstQuarter는_FIRST_HALF이다() {
        assertThat(SportType.SOCCER.firstQuarter()).isEqualTo(SoccerQuarter.FIRST_HALF);
    }

    @Test
    void 농구의_firstQuarter는_FIRST_QUARTER이다() {
        assertThat(SportType.BASKETBALL.firstQuarter()).isEqualTo(BasketballQuarter.FIRST_QUARTER);
    }

    @Test
    void 축구의_postGameQuarter는_SoccerQuarter_POST_GAME이다() {
        Quarter result = SportType.SOCCER.postGameQuarter();
        assertThat(result).isEqualTo(SoccerQuarter.POST_GAME);
        assertThat(result).isInstanceOf(SoccerQuarter.class);
    }

    @Test
    void 농구의_postGameQuarter는_BasketballQuarter_POST_GAME이다() {
        Quarter result = SportType.BASKETBALL.postGameQuarter();
        assertThat(result).isEqualTo(BasketballQuarter.POST_GAME);
        assertThat(result).isInstanceOf(BasketballQuarter.class);
    }

    @Test
    void PRE_GAME은_각_종목의_구현체로_resolve된다() {
        assertThat(SportType.SOCCER.resolveQuarter("PRE_GAME")).isInstanceOf(SoccerQuarter.class);
        assertThat(SportType.BASKETBALL.resolveQuarter("PRE_GAME")).isInstanceOf(BasketballQuarter.class);
    }
}
