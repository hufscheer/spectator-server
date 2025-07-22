package com.sports.server.query.dto.response;

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
