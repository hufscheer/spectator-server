package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LineupPlayerService {
    private final EntityUtils entityUtils;

    public void changePlayerStateToStarter(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.registerStarter(lineupPlayer);
    }

    public void changePlayerStateToCandidate(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.rollbackToCandidate(lineupPlayer);
    }

    public void changePlayerToCaptain(final Long gameId, final Long gameTeamId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        GameTeam gameTeam = entityUtils.getEntity(gameTeamId, GameTeam.class);
        LineupPlayer lineupPlayer = entityUtils.getEntity(lineupPlayerId, LineupPlayer.class);
        game.changePlayerToCaptain(gameTeam, lineupPlayer);
    }
}
