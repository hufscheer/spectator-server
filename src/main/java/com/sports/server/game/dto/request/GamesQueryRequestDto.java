package com.sports.server.game.dto.request;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

public record GamesQueryRequestDto(
        @RequestParam("league_id")
        Long leagueId,
        @RequestParam("state")
        String stateValue,
        @RequestParam("sport_id")
        List<Long> sportIds
) {
    private static final String DEFAULT_STATE_VALUE = "PLAYING";

    @Override
    public Long leagueId() {
        return leagueId;
    }

    @Override
    public String stateValue() {
        if (stateValue == null) {
            return DEFAULT_STATE_VALUE;
        }

        return stateValue;
    }

    @Override
    public List<Long> sportIds() {
        return sportIds;
    }
}
