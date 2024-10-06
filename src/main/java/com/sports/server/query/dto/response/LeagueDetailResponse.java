package com.sports.server.query.dto.response;

import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

public record LeagueDetailResponse(
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int maxRound,
        int inProgressRound,
        String leagueProgress,
        Integer leagueTeamCount
) {
	public static LeagueDetailResponse of(League league, Integer leagueTeamCount) {
		return new LeagueDetailResponse(
			league.getName(),
			league.getStartAt(),
			league.getEndAt(),
			league.getMaxRound().getNumber(),
			league.getInProgressRound().getNumber(),
			LeagueProgress.getProgressDescription(LocalDateTime.now(), league),
			leagueTeamCount
		);
	}
}
