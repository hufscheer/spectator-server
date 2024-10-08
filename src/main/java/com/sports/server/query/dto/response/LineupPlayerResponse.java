package com.sports.server.query.dto.response;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.domain.LineupPlayerState;

import java.util.List;

public class LineupPlayerResponse {

	public record Playing(
			Long gameTeamId,
			String teamName,
			List<PlayerResponse> gameTeamPlayers
	) {
		public Playing(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
			this(
					gameTeam.getId(),
					gameTeam.getLeagueTeam().getName(),
					lineupPlayers.stream()
							.map(PlayerResponse::new)
							.toList()
			);
		}
	}

	public record Separated(
			Long gameTeamId,
			String teamName,
			List<LineupPlayerResponse.PlayerResponse> starterPlayers,
			List<LineupPlayerResponse.PlayerResponse> candidatePlayers
	) {
		public Separated(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
			this(
					gameTeam.getId(),
					gameTeam.getLeagueTeam().getName(),
					lineupPlayers.stream()
							.filter(lineupPlayer -> lineupPlayer.getState().equals(LineupPlayerState.STARTER))
							.map(LineupPlayerResponse.PlayerResponse::new)
							.toList(),
					lineupPlayers.stream()
							.filter(lineupPlayer -> lineupPlayer.getState().equals(LineupPlayerState.CANDIDATE))
							.map(LineupPlayerResponse.PlayerResponse::new)
							.toList()
			);
		}
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