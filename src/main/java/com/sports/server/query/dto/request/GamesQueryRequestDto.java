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
    private String descriptionOfRound;

    public GamesQueryRequestDto(Long league_id, String state, List<Long> sport_id, List<Long> league_team_id,
                                String description_of_round) {
        this.leagueId = league_id;
        this.stateValue = state;
        this.sportIds = sport_id;
        this.leagueTeamIds = league_team_id;
        this.descriptionOfRound = description_of_round;
    }

    public String getStateValue() {
        if (stateValue == null) {
            return DEFAULT_STATE_VALUE;
        }

        return stateValue;
    }

}