package com.sports.server.query.dto.response;

import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

public record LeagueDetailResponse(
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String maxRound,
        String inProgressRound,
        String leagueProgress
) {

    public LeagueDetailResponse(League league) {
        this(
                league.getName(),
                league.getStartAt(),
                league.getEndAt(),
                league.getMaxRound().getDescription(),
                league.getInProgressRound().getDescription(),
                LeagueProgress.check(LocalDateTime.now(), league)
        );
    }
}
