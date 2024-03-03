package com.sports.server.query.application;

import static org.junit.Assert.assertEquals;

import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.ServiceTest;
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
}
