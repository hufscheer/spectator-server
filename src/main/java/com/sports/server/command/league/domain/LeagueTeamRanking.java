package com.sports.server.command.league.domain;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * 대회 안 팀 순위. 쓰기(통계 행의 1위 팀)와 읽기(응답 순위)가 같은 규칙을 쓴다.
 * 0 이하는 제외, 많은 순, 같으면 팀 이름과 팀 id 순. 공동 순위는 1,1,3 방식이고 3위까지 모두 낸다.
 */
public final class LeagueTeamRanking {

    public static final int MAX_RANKING = 3;

    private static final Comparator<LeagueTeamTally> NAME_THEN_ID = Comparator
            .comparing(LeagueTeamTally::teamName)
            .thenComparing(LeagueTeamTally::teamId);

    private LeagueTeamRanking() {
    }

    public record Ranked(int ranking, LeagueTeamTally tally, int count) {
    }

    public static List<Ranked> rank(List<LeagueTeamTally> tallies, ToIntFunction<LeagueTeamTally> countOf) {
        return tallies.stream()
                .filter(tally -> countOf.applyAsInt(tally) > 0)
                .sorted(Comparator.comparingInt(countOf).reversed().thenComparing(NAME_THEN_ID))
                .map(tally -> new Ranked(
                        1 + (int) tallies.stream()
                                .filter(other -> countOf.applyAsInt(other) > countOf.applyAsInt(tally))
                                .count(),
                        tally,
                        countOf.applyAsInt(tally)))
                .filter(ranked -> ranked.ranking() <= MAX_RANKING)
                .toList();
    }
}
