package com.sports.server.command.league.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorer;
import com.sports.server.command.league.domain.LeagueTopScorerRepository;
import com.sports.server.common.application.EntityUtils;
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
        @DisplayName("유효한 리그 ID가 주어지면 득점왕 정보가 업데이트된다")
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
        @DisplayName("존재하지 않는 리그 ID가 주어지면 예외가 발생한다")
        void 존재하지_않는_리그_ID가_주어지면_예외가_발생한다() {
            // given
            Long invalidLeagueId = 999L;

            // when & then
            assertThatThrownBy(() -> leagueTopScorerService.updateTopScorersForLeague(invalidLeagueId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("League not found");
        }

        @Test
        @DisplayName("리그의 득점왕이 순위별로 올바르게 저장된다")
        void 리그의_득점왕이_순위별로_올바르게_저장된다() {
            // given
            Long leagueId = 1L;

            // when
            leagueTopScorerService.updateTopScorersForLeague(leagueId);

            // then
            List<LeagueTopScorer> topScorers = leagueTopScorerRepository.findByLeagueId(leagueId);
            assertThat(topScorers).isNotEmpty();
            
            // 순위가 올바르게 설정되었는지 확인
            for (LeagueTopScorer topScorer : topScorers) {
                assertThat(topScorer.getRanking()).isPositive();
                assertThat(topScorer.getGoalCount()).isNotNegative();
                assertThat(topScorer.getPlayer()).isNotNull();
                assertThat(topScorer.getLeague().getId()).isEqualTo(leagueId);
            }
        }
    }
}