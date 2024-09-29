package com.sports.server.command.game.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineUpPlayerRepository;
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
    private final LineUpPlayerRepository lineUpPlayerRepository;

    public void changePlayerStateToStarter(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = lineUpPlayerRepository.findWithGameTeam(lineupPlayerId);
        game.registerStarter(lineupPlayer.getGameTeam(), lineupPlayer);
    }

    public void changePlayerStateToCandidate(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = lineUpPlayerRepository.findWithGameTeam(lineupPlayerId);
        game.rollbackToCandidate(lineupPlayer.getGameTeam(), lineupPlayer);
    }

    public void changePlayerToCaptain(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = lineUpPlayerRepository.findWithGameTeam(lineupPlayerId);
        game.changePlayerToCaptain(lineupPlayer.getGameTeam(), lineupPlayer);
    }

    public void revokeCaptainFromPlayer(final Long gameId, final Long lineupPlayerId) {
        Game game = entityUtils.getEntity(gameId, Game.class);
        LineupPlayer lineupPlayer = lineUpPlayerRepository.findWithGameTeam(lineupPlayerId);
        game.revokeCaptainFromPlayer(lineupPlayer.getGameTeam(), lineupPlayer);
    }
}
