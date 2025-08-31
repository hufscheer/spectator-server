package com.sports.server.query.dto.request;

import com.sports.server.command.league.domain.LeagueProgress;

public record LeagueQueryRequestDto(
        Integer year,
        LeagueProgress leagueProgress
) {
}
