package com.sports.server.command.game.application;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("dev")
@Sql(scripts = "/game-fixture.sql")
public class LineupPlayerServiceTest extends ServiceTest {

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private EntityUtils entityUtils;

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
}

