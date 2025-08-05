package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.LeagueTeamPlayer;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.TeamPlayer;

public record PlayerResponse(
	Long playerId,
	String name,
	String studentNumber,
	Integer jerseyNumber
) {
	public PlayerResponse(final Player player) {
		this(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null
		);
	}

	public static PlayerResponse of(final LeagueTeamPlayer leagueTeamPlayer) {
		Player player = leagueTeamPlayer.getPlayer();
		return new PlayerResponse(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				leagueTeamPlayer.getJerseyNumber()
		);
	}

	public static PlayerResponse of(final TeamPlayer teamPlayer) {
		Player player = teamPlayer.getPlayer();
		return new PlayerResponse(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null
		);
	}
}
