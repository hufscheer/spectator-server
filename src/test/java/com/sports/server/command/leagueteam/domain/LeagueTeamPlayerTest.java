package com.sports.server.command.leagueteam.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class LeagueTeamPlayerTest {
    @Test
    void 학번이_9자리_숫자가_아니면_예외를_던진다() {
        // given
        String invalidValue = "2020033";
        LeagueTeam leagueTeam = new LeagueTeam();

        // when & then
        assertThatThrownBy(() -> new LeagueTeamPlayer(leagueTeam, "진승희", 3, invalidValue))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
