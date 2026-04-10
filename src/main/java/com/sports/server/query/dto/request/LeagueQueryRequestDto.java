package com.sports.server.query.dto.request;

import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.command.league.domain.SportType;

public record LeagueQueryRequestDto(
        Integer year,
        LeagueProgress leagueProgress,
        SportType sportType
) {
}
