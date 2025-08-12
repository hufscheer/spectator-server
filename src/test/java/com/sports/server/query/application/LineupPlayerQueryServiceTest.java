package com.sports.server.query.application;

import static org.junit.Assert.assertEquals;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.LineupPlayerResponse;
import com.sports.server.support.ServiceTest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("dev")
@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerQueryServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerQueryService lineupPlayerQueryService;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private EntityUtils entityUtils;

    @Test
    void 라인업_조회시_게임팀_아이디의_오름차순으로_반환된다() {

        // given
        Long gameId = 1L;

        // when
        List<LineupPlayerResponse.All> responses = lineupPlayerQueryService.getLineup(gameId);

        // then
        List<Long> gameTeamIds = responses.stream().map(LineupPlayerResponse.All::gameTeamId).toList();
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
        List<LineupPlayerResponse.Playing> responses = lineupPlayerQueryService.getPlayingLineup(gameId);

        // then
        List<LineupPlayer> lineupPlayers = responses.stream()
                .flatMap(lpr -> lpr.gameTeamPlayers().stream()
                        .map(pr -> entityUtils.getEntity(pr.id(), LineupPlayer.class)))
                .toList();

        Assertions.assertThat(lineupPlayers)
                .map(LineupPlayer::isPlaying)
                .containsOnly(true);
    }

//    @Test
//    void 교체_타임라인이_등록되면_교체선수_정보가_등록된다() {
//
//        // given
//        Long gameId = 1L;
//        Long originLineupPlayerId = 2L;
//        Long replaceLineupPlayerId = 1L;
//        Member manager = entityUtils.getEntity(1L, Member.class);
//        TimelineRequest timelineRequest = new TimelineRequest.RegisterReplacement(1L, 1L,
//                originLineupPlayerId, replaceLineupPlayerId, 2);
//        timelineService.register(manager, gameId, timelineRequest);
//
//        // when
//        List<LineupPlayerResponse.All> responses = lineupPlayerQueryService.getLineup(gameId);
//
//        // then
//        Long replacedPlayerId = responses.get(0).candidatePlayers().stream()
//                .filter(playerResponse -> playerResponse.id().equals(replaceLineupPlayerId))
//                .map(player -> player.replacedPlayer().id())
//                .findFirst()
//                .orElse(null); // 값이 없으면 null 반환
//        Assertions.assertThat(originLineupPlayerId).isEqualTo(replacedPlayerId);
//
//    }
}
