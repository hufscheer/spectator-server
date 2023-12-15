package com.sports.server.command.league.dto.response;

import com.sports.server.command.league.domain.LeagueSport;

public record LeagueSportResponse(
        Long sportId,
        String name
) {
    public LeagueSportResponse(LeagueSport leagueSport) {
        this(
                leagueSport.getSport().getId(),
                leagueSport.getSport().getName()
        );
    }
}
