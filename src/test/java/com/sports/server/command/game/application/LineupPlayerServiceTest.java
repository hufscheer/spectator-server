package com.sports.server.command.game.application;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.game.domain.Position;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.CustomException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 게임팀 1(축구)은 선발 4명이다. 선발 수가 total 이 되도록 이미 라인업에 없는 선수(6~10, 16~)로 채운다
    private void fillStarters(int total) {
        for (int i = 0; i < total - 4; i++) {
            jdbcTemplate.update(
                    "INSERT INTO lineup_players (id, game_team_id, player_id, jersey_number, is_captain, state, is_playing) "
                            + "VALUES (?, 1, ?, ?, false, 'STARTER', true)", 100 + i, i < 5 ? 6 + i : 11 + i, 50 + i);
        }
    }

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
                7L, LineupPlayerState.STARTER, true, null
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
                1L, LineupPlayerState.STARTER, true, null
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
                7L, LineupPlayerState.STARTER, false, null
        );

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId, request);

        // then
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(added.isCaptain()).isEqualTo(false);
    }

    @Test
    void 라인업에_추가할_때_요청한_포지션이_저장된다() {
        // given
        Long gameTeamId = 1L;

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, Position.ST));

        // then
        LineupPlayer added = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(added.getPosition()).isEqualTo(Position.ST);
    }

    @Test
    void 포지션은_선택_입력이라_없이도_추가된다() {
        // given
        Long gameTeamId = 1L;

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, null));

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getPosition()).isNull();
    }

    @Test
    void 경기_종목에_없는_포지션이면_예외가_발생한다() {
        // given: 축구 경기에 농구 포지션
        Long gameTeamId = 1L;

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.addPlayerToLineup(gameTeamId,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, Position.PG)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 라인업_선수의_포지션을_수정한다() {
        // given
        Long gameId = 1L;
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(1L,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, Position.ST));

        // when
        lineupPlayerService.changePlayerPosition(gameId, lineupPlayerId, Position.LW);

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getPosition())
                .isEqualTo(Position.LW);
    }

    @Test
    void 포지션을_null_로_보내면_해제된다() {
        // given
        Long gameId = 1L;
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(1L,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, Position.ST));

        // when
        lineupPlayerService.changePlayerPosition(gameId, lineupPlayerId, null);

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getPosition()).isNull();
    }

    @Test
    void 다른_경기의_라인업선수는_수정할_수_없다() {
        // given: gameTeamId 1 은 gameId 1 에 속한다
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(1L,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, Position.ST));

        // when & then: 다른 경기 id 로는 건드릴 수 없다
        assertThatThrownBy(() -> lineupPlayerService.changePlayerPosition(2L, lineupPlayerId, Position.LW))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 선발이_정원만큼_찬_팀에서_후보를_선발로_바꾸면_예외가_발생한다() {
        // given: 축구 정원 11명
        fillStarters(11);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.changePlayerStateToStarter(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("선발 선수는 11명까지 등록할 수 있어요.");
    }

    @Test
    void 선발이_정원보다_한_명_모자란_팀에서는_후보를_선발로_바꿀_수_있다() {
        // given
        fillStarters(10);

        // when
        lineupPlayerService.changePlayerStateToStarter(1L, 1L);

        // then
        assertThat(entityUtils.getEntity(1L, LineupPlayer.class).getState()).isEqualTo(LineupPlayerState.STARTER);
    }

    @Test
    void 이미_선발인_선수를_선발로_바꾸면_정원과_관계없이_이미_선발_예외가_발생한다() {
        // given
        fillStarters(11);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.changePlayerStateToStarter(1L, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 선발로 등록된 선수입니다.");
    }

    @Test
    void 선발이_정원만큼_찬_팀에_선발을_추가하면_예외가_발생한다() {
        // given
        fillStarters(11);
        GameRequest.LineupPlayerRequest request = new GameRequest.LineupPlayerRequest(
                1L, LineupPlayerState.STARTER, false, null
        );

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.addPlayerToLineup(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("선발 선수는 11명까지 등록할 수 있어요.");
    }

    @Test
    void 선발이_정원만큼_찬_팀에도_후보는_추가할_수_있다() {
        // given
        fillStarters(11);

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(1L,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.CANDIDATE, false, null));

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getState())
                .isEqualTo(LineupPlayerState.CANDIDATE);
    }

    @Test
    void 선발이_정원보다_한_명_모자란_팀에는_선발을_추가할_수_있다() {
        // given
        fillStarters(10);

        // when
        Long lineupPlayerId = lineupPlayerService.addPlayerToLineup(1L,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.STARTER, false, null));

        // then
        assertThat(entityUtils.getEntity(lineupPlayerId, LineupPlayer.class).getState())
                .isEqualTo(LineupPlayerState.STARTER);
    }

    @Test
    void 정원을_넘긴_팀에서도_선발을_후보로_바꾸고_삭제할_수_있다() {
        // given: 이미 12명이 선발인 기존 데이터
        fillStarters(12);

        // when
        lineupPlayerService.changePlayerStateToCandidate(1L, 2L);
        lineupPlayerService.removePlayerFromLineup(1L, 3L);

        // then
        assertThat(entityUtils.getEntity(2L, LineupPlayer.class).getState()).isEqualTo(LineupPlayerState.CANDIDATE);
    }
}

