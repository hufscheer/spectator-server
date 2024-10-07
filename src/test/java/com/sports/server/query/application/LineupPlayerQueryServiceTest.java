package com.sports.server.query.application;

import static org.junit.Assert.assertEquals;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.query.dto.response.LineupPlayerResponseSeparated;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerQueryServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerQueryService lineupPlayerQueryService;

    @Autowired
    private EntityUtils entityUtils;

    @Test
    void 라인업_조회시_게임팀_아이디의_오름차순으로_반환된다() {

        // given
        Long gameId = 1L;

        // when
        List<LineupPlayerResponseSeparated> responses = lineupPlayerQueryService.getLineup(gameId);

        // then
        List<Long> gameTeamIds = responses.stream().map(LineupPlayerResponseSeparated::gameTeamId).toList();
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

        // when
        List<LineupPlayerResponse> responses = lineupPlayerQueryService.getPlayingLineup(gameId);

        // then
        List<LineupPlayer> lineupPlayers = responses.stream()
                .flatMap(lpr -> lpr.gameTeamPlayers().stream()
                        .map(pr -> entityUtils.getEntity(pr.id(), LineupPlayer.class)))
                .toList();

        Assertions.assertThat(lineupPlayers)
                .map(LineupPlayer::isPlaying)
                .containsOnly(true);
    }
}
