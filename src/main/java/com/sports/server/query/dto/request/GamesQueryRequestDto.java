package com.sports.server.query.dto.request;

import java.util.List;
import lombok.Getter;

@Getter
public class GamesQueryRequestDto {

    private static final String DEFAULT_STATE_VALUE = "PLAYING";

    private Long leagueId;
    private String stateValue;
    private List<Long> sportIds;
    private List<Long> leagueTeamIds;
    private Integer round;

    public GamesQueryRequestDto(Long league_id, String state, List<Long> sport_id, List<Long> league_team_id,
                                Integer round) {
        this.leagueId = league_id;
        this.stateValue = state;
        this.sportIds = sport_id;
        this.leagueTeamIds = league_team_id;
        this.round = round;
    }

    public String getStateValue() {
        if (stateValue == null) {
            return DEFAULT_STATE_VALUE;
        }

        return stateValue;
    }

}