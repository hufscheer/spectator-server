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
					gameTeam.getTeam().getName(),
					lineupPlayers.stream()
							.map(PlayerResponse::new)
							.toList()
			);
		}
	}

	public record All(
			Long gameTeamId,
			String teamName,
			List<PlayerResponse> starterPlayers,
			List<PlayerResponse> candidatePlayers
	) {
		public All(GameTeam gameTeam, List<LineupPlayer> lineupPlayers) {
			this(
					gameTeam.getId(),
					gameTeam.getTeam().getName(),
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
			LineupPlayerState state,
			boolean isReplaced,
			PlayerSummary replacedPlayer
	) {
		public PlayerResponse(LineupPlayer lineupPlayer) {
			this(
					lineupPlayer.getId(),
					lineupPlayer.getLeagueTeamPlayer().getPlayer().getName(),
					lineupPlayer.getDescription(),
					lineupPlayer.getJerseyNumber(),
					lineupPlayer.isCaptain(),
					lineupPlayer.getState(),
					lineupPlayer.isReplaced(),
					lineupPlayer.getReplacedPlayer() != null ? new PlayerSummary(lineupPlayer.getReplacedPlayer()) : null
			);
		}
	}

	public record PlayerSummary(
			Long id,
			String playerName,
			int number
	) {
		public PlayerSummary(LineupPlayer lineupPlayer) {
			this(
					lineupPlayer.getId(),
					lineupPlayer.getLeagueTeamPlayer().getPlayer().getName(),
					lineupPlayer.getJerseyNumber());
		}
	}
}