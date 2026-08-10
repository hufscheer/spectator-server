package com.sports.server.query.dto.request;

import java.util.List;
import lombok.Getter;

@Getter
public class GamesQueryRequestDto {

    private static final String DEFAULT_STATE_VALUE = "PLAYING";
    private static final int DEFAULT_ROUND_VALUE = 0;

    private Long leagueId;
    private String stateValue;
    private List<Long> leagueTeamIds;
    private Integer round;
    private Boolean thirdPlace;

    public GamesQueryRequestDto(Long league_id, String state, List<Long> league_team_id, Integer round,
                                Boolean third_place) {
        this.leagueId = league_id;
        this.stateValue = state;
        this.leagueTeamIds = league_team_id;
        this.round = round;
        this.thirdPlace = third_place;
    }

    public String getStateValue() {
        if (stateValue == null) {
            return DEFAULT_STATE_VALUE;
        }
        return stateValue;
    }

    public int getRound() {
        if (round == null) {
            return DEFAULT_ROUND_VALUE;
        }
        return round;
    }

    public boolean isThirdPlace() {
        return Boolean.TRUE.equals(thirdPlace);
    }

}