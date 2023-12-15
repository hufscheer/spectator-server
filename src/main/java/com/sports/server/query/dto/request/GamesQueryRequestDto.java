package com.sports.server.query.dto.request;

import java.util.List;
import lombok.Getter;

@Getter
public class GamesQueryRequestDto {

    private static final String DEFAULT_STATE_VALUE = "PLAYING";

    private Long leagueId;
    private String stateValue;
    private List<Long> sportIds;

    public GamesQueryRequestDto(Long league_id, String status, List<Long> sport_id) {
        this.leagueId = league_id;
        this.stateValue = status;
        this.sportIds = sport_id;
    }

    public String getStateValue() {
        if (stateValue == null) {
            return DEFAULT_STATE_VALUE;
        }

        return stateValue;
    }

}