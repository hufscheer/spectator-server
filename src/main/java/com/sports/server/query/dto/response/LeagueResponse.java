package com.sports.server.query.dto.response;

import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

public record LeagueResponse(
	Long leagueId,
	String name,
	int maxRound,
	int inProgressRound,
	String leagueProgress
) {
	public LeagueResponse(League league) {
		this(
			league.getId(),
			league.getName(),
			league.getMaxRound().getNumber(),
			league.getInProgressRound().getNumber(),
			LeagueProgress.getProgressDescription(LocalDateTime.now(), league)
		);
	}
}
