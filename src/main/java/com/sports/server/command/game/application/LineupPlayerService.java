package com.sports.server.command.game.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.application.EntityUtils;

import lombok.RequiredArgsConstructor;

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
}
