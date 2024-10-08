package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;

import java.util.List;

public record LineupPlayerResponseSeparated(
	Long gameTeamId,
	String teamName,
	List<PlayerResponse> starterPlayers,
	List<PlayerResponse> candidatePlayers

) {

	public LineupPlayerResponseSeparated(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
		this(
			gameTeam.getId(),
			gameTeam.getLeagueTeam().getName(),
			lineupPlayers.stream()
					.filter(lineupPlayer -> lineupPlayer.getState().equals(LineupPlayerState.STARTER))
					.map(PlayerResponse::new)
					.toList(),
			lineupPlayers.stream()
					.filter(lineupPlayer -> lineupPlayer.getState().equals(LineupPlayerState.CANDIDATE))
					.map(PlayerResponse::new)
					.toList()
		);
	}

	public record PlayerResponse(
		Long id,
		String playerName,
		String description,
		int number,
		boolean isCaptain,
		LineupPlayerState state
	) {
		public PlayerResponse(LineupPlayer player) {
			this(player.getId(), player.getName(), player.getDescription(), player.getNumber(), player.isCaptain(),
				player.getState());
		}
	}
}
