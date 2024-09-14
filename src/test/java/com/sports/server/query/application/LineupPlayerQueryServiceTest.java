package com.sports.server.query.application;

import static org.junit.Assert.assertEquals;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.query.dto.response.LineupPlayerResponse.PlayerResponse;
import com.sports.server.support.ServiceTest;
import com.sports.server.support.fixture.LineupPlayerFixtureRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerQueryServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerQueryService lineupPlayerQueryService;

    @Autowired
    private LineupPlayerFixtureRepository lineupPlayerFixtureRepository;

    @Test
    void 라인업_조회시_게임팀_아이디의_오름차순으로_반환된다() {

        // given
        Long gameId = 1L;

        // when
        List<LineupPlayerResponse> responses = lineupPlayerQueryService.getLineup(gameId);

        // then
        List<Long> gameTeamIds = responses.stream().map(LineupPlayerResponse::gameTeamId).toList();
        List<Long> sortedGameTeamIds = gameTeamIds.stream()
                .sorted(Comparator.comparingLong(Long::valueOf))
                .collect(Collectors.toList());

        assertEquals(
                gameTeamIds, sortedGameTeamIds
        );

    }

    @Test
    void 출전_선수_조회시_출전상태인_선수들만_조회된다() {

        // given
        Long gameId = 1L;
        List<LineupPlayer> playingPlayers = lineupPlayerFixtureRepository.findPlayingPlayersByGameId(gameId);

        // when
        List<LineupPlayerResponse> responses = lineupPlayerQueryService.getPlayingLineup(gameId);

        // then
        assertEquals(playingPlayers.stream().map(LineupPlayer::getId).toList(), responses.stream()
                .flatMap(lp -> lp.gameTeamPlayers().stream().map(PlayerResponse::id))
                .collect(Collectors.toList()));
    }
}
