package com.sports.server.query.application;

import com.sports.server.query.dto.response.GameTeamCheerResponseDto;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/game-fixture.sql")
class GameLeagueTeamQueryServiceTest extends ServiceTest {

    @Autowired
    private GameTeamQueryService gameTeamQueryService;

    @Test
    void 경기에_참여하는_팀의_순서가_알맞게_반환된다() {

        //given
        Long gameId = 1L;

        //when
        List<GameTeamCheerResponseDto> cheerCountOfGameTeams = gameTeamQueryService.getCheerCountOfGameTeams(gameId);

        // then
        assertThat(cheerCountOfGameTeams).isSortedAccordingTo(
                Comparator.comparingLong(GameTeamCheerResponseDto::gameTeamId));

        int expectedOrder = 1;
        for (GameTeamCheerResponseDto responseDto : cheerCountOfGameTeams) {
            assertThat(responseDto.order()).isEqualTo(expectedOrder);
            expectedOrder++;
        }
    }
}
