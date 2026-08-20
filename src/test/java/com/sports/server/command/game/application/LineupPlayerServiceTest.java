package com.sports.server.command.game.application;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.team.domain.Position;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @Test
    void 선수를_주장으로_등록한다() {
        // given
        Long gameId = 1L;
        Long lineupPlayerId = 1L;

        // when
        lineupPlayerService.changePlayerToCaptain(gameId, lineupPlayerId);

        // then
        LineupPlayer changedLineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(changedLineupPlayer.isCaptain()).isEqualTo(true);
    }

    @Test
    void 선수를_주장에서_해제한다() {
        // given
        Long gameId = 1L;
        Long lineupPlayerId = 6L;

        // when
        lineupPlayerService.revokeCaptainFromPlayer(gameId, lineupPlayerId);

        // then
        LineupPlayer changedLineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(changedLineupPlayer.isCaptain()).isEqualTo(false);
    }

    @Test
    void 주장으로_등록된_선수를_후보로_변경한다() {
        // given
        Long gameId = 1L;
        Long lineupPlayerId = 6L;

        // when
        lineupPlayerService.changePlayerStateToCandidate(gameId, lineupPlayerId);

        // then
        LineupPlayer changedLineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertAll(
                () -> assertThat(changedLineupPlayer.isCaptain()).isEqualTo(false),
                () -> assertThat(changedLineupPlayer.getState()).isEqualTo(LineupPlayerState.CANDIDATE)
        );
    }

    @Test
    void 이미_주장이_있는_팀에는_주장을_추가할_수_없다() {
        // given
        Long gameTeamId = 2L;
        GameRequest.LineupPlayerRequest request = new GameRequest.LineupPlayerRequest(
                7L, LineupPlayerState.STARTER, true
        );

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.addPlayerToLineup(gameTeamId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 주장이 등록된 팀입니다. 기존 주장을 먼저 해제한 뒤 추가하세요.");
    }

    @Test
    void 주장이_없는_팀에_주장을_추가한다() {
        // given
        Long gameTeamId = 1L;
        GameRequest.LineupPlayerRequest request = new GameRequest.LineupPlayerRequest(
                1L, LineupPlayerState.STARTER, true
        );

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId, request);

        // then
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(added.isCaptain()).isEqualTo(true);
    }

    @Test
    void 주장이_있는_팀에_일반_선수는_추가할_수_있다() {
        // given
        Long gameTeamId = 2L;
        GameRequest.LineupPlayerRequest request = new GameRequest.LineupPlayerRequest(
                7L, LineupPlayerState.STARTER, false
        );

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId, request);

        // then
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(added.isCaptain()).isEqualTo(false);
    }

    @Test
    void 라인업에_추가하면_로스터의_포지션이_복사된다() {
        // given
        Long gameTeamId = 1L;
        Long teamPlayerId = 1L;
        updateRosterPosition(teamPlayerId, Position.ST);

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(teamPlayerId, LineupPlayerState.STARTER, false));

        // then
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(added.getPosition()).isEqualTo(Position.ST);
    }

    @Test
    void 라인업_반영_후_로스터_포지션이_바뀌어도_경기_기록은_유지된다() {
        // given
        Long gameTeamId = 1L;
        Long teamPlayerId = 1L;
        updateRosterPosition(teamPlayerId, Position.ST);
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(teamPlayerId, LineupPlayerState.STARTER, false));

        // when: 이후 로스터에서 포지션을 바꾼다
        updateRosterPosition(teamPlayerId, Position.GK);

        // then: 이미 만들어진 라인업은 당시 포지션을 유지한다
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertAll(
                () -> assertThat(added.getPosition()).isEqualTo(Position.ST),
                () -> assertThat(teamPlayerRepository.findById(teamPlayerId).orElseThrow().getPosition())
                        .isEqualTo(Position.GK)
        );
    }

    @Test
    void 로스터에_포지션이_없으면_라인업에도_포지션이_없다() {
        // given: 픽스처의 team_player 는 포지션 미등록 상태다
        Long gameTeamId = 1L;

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false));

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getPosition()).isNull();
    }

    private void updateRosterPosition(Long teamPlayerId, Position position) {
        TeamPlayer teamPlayer = teamPlayerRepository.findById(teamPlayerId).orElseThrow();
        teamPlayer.updatePosition(position);
        teamPlayerRepository.save(teamPlayer);
    }
}

