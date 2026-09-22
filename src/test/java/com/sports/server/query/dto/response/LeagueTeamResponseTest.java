package com.sports.server.query.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.sports.server.command.league.domain.LeagueTeam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("대회 통계의 팀 응답은")
class LeagueTeamResponseTest {

    /*
     * 운영 500 회귀. `league_statistics` 는 팀을 FK 로 들고 있는데 Team 에 @Where(is_deleted = 0)
     * 이 걸려 있어, 그 팀이 소프트 삭제되면 LEFT JOIN FETCH 가 연관을 null 로 채운다.
     * FK 값은 남아 있어서 DB 만 보면 멀쩡해 보이고, 화면에서만 터졌다.
     */
    @Test
    void 지워진_팀이면_응원_수를_값_없이_돌려준다() {
        assertThat(LeagueTeamResponse.ofWithCheerCount(null)).isNull();
    }

    @Test
    void 지워진_팀이면_응원톡_수를_값_없이_돌려준다() {
        assertThat(LeagueTeamResponse.ofWithTotalTalkCount(null)).isNull();
    }

    @Test
    void 지워진_팀이어도_예외를_던지지_않는다() {
        LeagueTeam deleted = null;

        assertThatCode(() -> {
            LeagueTeamResponse.ofWithCheerCount(deleted);
            LeagueTeamResponse.ofWithTotalTalkCount(deleted);
        }).doesNotThrowAnyException();
    }
}
