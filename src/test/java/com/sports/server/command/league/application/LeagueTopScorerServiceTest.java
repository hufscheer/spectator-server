package com.sports.server.command.league.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@Sql("/timeline-fixture.sql")
public class LeagueTopScorerServiceTest extends ServiceTest {

    @Autowired
    private LeagueTopScorerService leagueTopScorerService;

    @Autowired
    private LeagueTopScorerRepository leagueTopScorerRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Nested
    @DisplayName("리그 득점왕을 업데이트할 때")
    class UpdateTopScorersForLeagueTest {

        @Test
        void 유효한_리그_ID가_주어지면_득점왕_정보가_업데이트된다() {
            // given
            Long leagueId = 1L;
            League league = entityUtils.getEntity(leagueId, League.class);

            // when
            leagueTopScorerService.updateTopScorersForLeague(leagueId);

            // then
            List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(leagueId);
            assertThat(topScorers).isNotEmpty();
        }

        @Test
        void 존재하지_않는_리그_ID가_주어지면_예외가_발생한다() {
            // given
            Long invalidLeagueId = 999L;

            // when & then
            assertThatThrownBy(() -> leagueTopScorerService.updateTopScorersForLeague(invalidLeagueId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("League을(를) 찾을 수 없습니다");
        }

        @Test
        void 리그의_득점왕이_순위별로_올바르게_저장된다() {
            // given
            Long leagueId = 1L;

            // when
            leagueTopScorerService.updateTopScorersForLeague(leagueId);

            // then
            List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(leagueId);
            assertThat(topScorers).isNotEmpty();

            for (int i = 0; i < topScorers.size() - 1; i++) {
                LeagueTopScorer current = topScorers.get(i);
                LeagueTopScorer next = topScorers.get(i + 1);

                // 골 수가 내림차순으로 정렬되었는지 확인
                assertThat(current.getGoalCount()).isGreaterThanOrEqualTo(next.getGoalCount());

                // 동점이 아닌 경우 순위가 증가하는지 확인
                if (!current.getGoalCount().equals(next.getGoalCount())) {
                    assertThat(next.getRanking()).isGreaterThan(current.getRanking());
                }
                // 동점인 경우 같은 순위인지 확인
                else {
                    assertThat(next.getRanking()).isEqualTo(current.getRanking());
                }
            }
        }

        @Test
        void 다른_리그의_득점은_집계에_포함되지_않는다() {
            // given
            Long league1Id = 1L;

            // when
            leagueTopScorerService.updateTopScorersForLeague(league1Id);

            // then
            List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(league1Id);

            LeagueTopScorer player1TopScorer = topScorers.stream()
                    .filter(scorer -> scorer.getPlayer().getId().equals(1L))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("선수1이 득점왕 목록에 없습니다."));
            assertThat(player1TopScorer.getGoalCount()).isEqualTo(2); // 리그1에서만 2골

            LeagueTopScorer player2TopScorer = topScorers.stream()
                    .filter(scorer -> scorer.getPlayer().getId().equals(2L))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("선수2가 득점왕 목록에 없습니다."));
            assertThat(player2TopScorer.getGoalCount()).isEqualTo(2); // 리그1에서만 2골
        }
    }
}