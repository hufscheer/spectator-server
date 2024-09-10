package com.sports.server.command.game.application;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.application.EntityUtils;
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

    @Test
    void 선수를_주장으로_등록한다() {
        // given
        Long gameId = 1L;
        Long gameTeamId = 1L;
        Long lineupPlayerId = 1L;

        // when
        lineupPlayerService.changePlayerToCaptain(gameId, gameTeamId, lineupPlayerId);

        // then
        LineupPlayer changedLineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        assertThat(changedLineupPlayer.isCaptain()).isEqualTo(true);
    }
}

