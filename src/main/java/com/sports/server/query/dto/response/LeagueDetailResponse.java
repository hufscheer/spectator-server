package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.League;
import com.sports.server.query.application.InProgressLeagueChecker;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeagueDetailResponse(
        String name,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer maxRound,
        Integer inProgressRound,
        Boolean isInProgress
) {

    public LeagueDetailResponse(League league) {
        this(
                league.getName(),
                league.getStartAt(),
                league.getEndAt(),
                league.getMaxRound(),
                league.getInProgressRound(),
                InProgressLeagueChecker.check(LocalDate.now(), league)
        );
    }
}
