package com.sports.server.query.dto.response;

import com.sports.server.command.leagueteam.domain.LeagueTeamPlayer;

public record LeagueTeamPlayerResponse(
	Long id,
	String name,
	String description,
	Integer number,
	String studentNumber
) {
	public LeagueTeamPlayerResponse(LeagueTeamPlayer leagueTeamPlayer) {
		this(
			leagueTeamPlayer.getId(),
			leagueTeamPlayer.getName(),
			leagueTeamPlayer.getDescription(),
			leagueTeamPlayer.getNumber(),
				leagueTeamPlayer.getStudentNumber()
		);
	}
}
