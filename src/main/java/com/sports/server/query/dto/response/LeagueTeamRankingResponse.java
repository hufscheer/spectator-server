package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.league.domain.LeagueTeamRanking;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeagueTeamRankingResponse(
        Integer ranking,
        Long teamId,
        Long leagueTeamId,
        String teamName,
        String logoImageUrl,
        Integer cheerCount,
        Integer cheerTalksCount
) {
    public static LeagueTeamRankingResponse ofCheerCount(LeagueTeamRanking.Ranked ranked) {
        return new LeagueTeamRankingResponse(
                ranked.ranking(), ranked.tally().teamId(), ranked.tally().leagueTeamId(),
                ranked.tally().teamName(), ranked.tally().logoImageUrl(), ranked.count(), null);
    }

    public static LeagueTeamRankingResponse ofCheerTalksCount(LeagueTeamRanking.Ranked ranked) {
        return new LeagueTeamRankingResponse(
                ranked.ranking(), ranked.tally().teamId(), ranked.tally().leagueTeamId(),
                ranked.tally().teamName(), ranked.tally().logoImageUrl(), null, ranked.count());
    }
}
