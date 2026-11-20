package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("대회 팀 순위는")
class LeagueTeamRankingTest {

    @Test
    void 많은_순으로_1위부터_매긴다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(tally(1, "A", 10), tally(2, "B", 30), tally(3, "C", 20));

        assertThat(ranked).extracting(r -> r.tally().teamId()).containsExactly(2L, 3L, 1L);
        assertThat(ranked).extracting(LeagueTeamRanking.Ranked::ranking).containsExactly(1, 2, 3);
    }

    @Test
    void 동률이면_공동_순위이고_다음_순위는_건너뛴다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(tally(1, "A", 100), tally(2, "B", 100), tally(3, "C", 50));

        assertThat(ranked).extracting(LeagueTeamRanking.Ranked::ranking).containsExactly(1, 1, 3);
    }

    @Test
    void 동률이면_팀_이름_그다음_팀_id_순으로_정렬한다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(tally(3, "나", 5), tally(2, "가", 5), tally(1, "가", 5));

        assertThat(ranked).extracting(r -> r.tally().teamId()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void 삼위_경계에서_동률이면_모두_포함한다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(
                tally(1, "A", 100), tally(2, "B", 90), tally(3, "C", 80), tally(4, "D", 80), tally(5, "E", 70));

        assertThat(ranked).hasSize(4);
        assertThat(ranked).extracting(LeagueTeamRanking.Ranked::ranking).containsExactly(1, 2, 3, 3);
    }

    @Test
    void 사위_이하는_제외한다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(
                tally(1, "A", 100), tally(2, "B", 90), tally(3, "C", 80), tally(4, "D", 70));

        assertThat(ranked).extracting(r -> r.tally().teamId()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void 일등이_둘이면_삼위가_바로_이어진다() {
        List<LeagueTeamRanking.Ranked> ranked = rank(
                tally(1, "A", 100), tally(2, "B", 100), tally(3, "C", 100), tally(4, "D", 90));

        assertThat(ranked).extracting(LeagueTeamRanking.Ranked::ranking).containsExactly(1, 1, 1);
    }

    @Test
    void 영_이하는_제외하고_전부_영이면_비어_있다() {
        assertThat(rank(tally(1, "A", 0), tally(2, "B", 0))).isEmpty();
        assertThat(rank(tally(1, "A", 0), tally(2, "B", 5))).hasSize(1);
    }

    private List<LeagueTeamRanking.Ranked> rank(LeagueTeamTally... tallies) {
        return LeagueTeamRanking.rank(List.of(tallies), LeagueTeamTally::cheerCount);
    }

    private LeagueTeamTally tally(long id, String name, int cheerCount) {
        return new LeagueTeamTally(id, id, name, "logo", cheerCount, 0);
    }
}
