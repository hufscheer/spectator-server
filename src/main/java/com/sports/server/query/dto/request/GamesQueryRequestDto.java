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

    public GamesQueryRequestDto(Long leagueId, String state, List<Long> leagueTeamIds, Integer round) {
        this.leagueId = leagueId;
        this.stateValue = state;
        this.leagueTeamIds = leagueTeamIds;
        this.round = round;
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

}