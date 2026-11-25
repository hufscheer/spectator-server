package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.command.game.domain.Position;
import com.sports.server.command.game.dto.GameRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.ForbiddenException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

// 경기 1은 회원 1이 관리한다. 회원 2는 그 경기를 관리하지 않는다
@Sql(scripts = "/game-fixture.sql")
@DisplayName("라인업 변경은 경기 관리자만 할 수 있다")
class LineupPlayerOwnershipTest extends ServiceTest {

    private static final Long GAME_ID = 1L;
    private static final Long GAME_TEAM_ID = 1L;

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member other;

    @BeforeEach
    void setUp() {
        other = entityUtils.getEntity(2L, Member.class);
    }

    private long lineupCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lineup_players", Long.class);
    }

    @Test
    void 선발로_바꿀_수_없다() {
        assertThatThrownBy(() -> lineupPlayerService.changePlayerStateToStarter(GAME_ID, 1L, other))
                .isInstanceOf(ForbiddenException.class);
        assertThat(entityUtils.getEntity(1L, LineupPlayer.class).getState()).isEqualTo(LineupPlayerState.CANDIDATE);
    }

    @Test
    void 후보로_바꿀_수_없다() {
        assertThatThrownBy(() -> lineupPlayerService.changePlayerStateToCandidate(GAME_ID, 2L, other))
                .isInstanceOf(ForbiddenException.class);
        assertThat(entityUtils.getEntity(2L, LineupPlayer.class).getState()).isEqualTo(LineupPlayerState.STARTER);
    }

    @Test
    void 포지션을_바꿀_수_없다() {
        assertThatThrownBy(() -> lineupPlayerService.changePlayerPosition(GAME_ID, 2L, Position.LW, other))
                .isInstanceOf(ForbiddenException.class);
        assertThat(entityUtils.getEntity(2L, LineupPlayer.class).getPosition()).isNull();
    }

    @Test
    void 주장으로_등록할_수_없다() {
        assertThatThrownBy(() -> lineupPlayerService.changePlayerToCaptain(GAME_ID, 1L, other))
                .isInstanceOf(ForbiddenException.class);
        assertThat(entityUtils.getEntity(1L, LineupPlayer.class).isCaptain()).isFalse();
    }

    @Test
    void 주장을_해제할_수_없다() {
        assertThatThrownBy(() -> lineupPlayerService.revokeCaptainFromPlayer(GAME_ID, 6L, other))
                .isInstanceOf(ForbiddenException.class);
        assertThat(entityUtils.getEntity(6L, LineupPlayer.class).isCaptain()).isTrue();
    }

    @Test
    void 라인업에_선수를_추가할_수_없다() {
        long before = lineupCount();

        assertThatThrownBy(() -> lineupPlayerService.addPlayerToLineup(GAME_TEAM_ID,
                new GameRequest.LineupPlayerRequest(1L, LineupPlayerState.CANDIDATE, false, null), other))
                .isInstanceOf(ForbiddenException.class);

        assertThat(lineupCount()).isEqualTo(before);
    }

    @Test
    void 라인업에서_선수를_뺄_수_없다() {
        long before = lineupCount();

        assertThatThrownBy(() -> lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, 1L, other))
                .isInstanceOf(ForbiddenException.class);

        assertThat(lineupCount()).isEqualTo(before);
    }
}
