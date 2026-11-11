package com.sports.server.command.league.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.bracket.domain.BracketMatchRepository;
import com.sports.server.query.repository.LeagueTeamQueryRepository;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

// 리그 id 로 거르는 조회가 리그 soft delete 와 상관없이 행을 돌려주는지 본다.
// 파생 쿼리(findByLeagueId)는 Hibernate 6.6 부터 leagues 를 조인해 삭제 조건을 붙여서 결과가 비었다
@Sql({"/bracket-fixture.sql", "/soft-deleted-league-fixture.sql"})
class SoftDeletedLeagueQueryTest extends ServiceTest {

    private static final Long DELETED_LEAGUE_ID = 3L;

    @Autowired
    private LeagueTopScorerRepository leagueTopScorerRepository;

    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Autowired
    private LeagueTeamQueryRepository leagueTeamQueryRepository;

    @Autowired
    private LeagueStatisticsRepository leagueStatisticsRepository;

    @Autowired
    private BracketMatchRepository bracketMatchRepository;

    @Test
    void 삭제된_리그도_리그_id로_연관_행을_조회한다() {
        assertThat(leagueTopScorerRepository.findByLeagueId(DELETED_LEAGUE_ID))
                .hasSize(1)
                .allSatisfy(lts -> assertThat(lts.getLeague().getId()).isEqualTo(DELETED_LEAGUE_ID));
        assertThat(leagueTeamRepository.findByLeagueId(DELETED_LEAGUE_ID))
                .hasSize(4)
                .allSatisfy(lt -> assertThat(lt.getLeague().getId()).isEqualTo(DELETED_LEAGUE_ID));
        assertThat(leagueTeamQueryRepository.findByLeagueId(DELETED_LEAGUE_ID))
                .hasSize(4)
                .allSatisfy(lt -> assertThat(lt.getLeague().getId()).isEqualTo(DELETED_LEAGUE_ID));
        assertThat(leagueStatisticsRepository.findByLeagueId(DELETED_LEAGUE_ID)).isNotNull();
        assertThat(bracketMatchRepository.existsByLeagueIdAndGameIsNotNull(DELETED_LEAGUE_ID)).isTrue();
        assertThat(bracketMatchRepository.existsByLeagueIdAndRoundAndGameIsNotNull(
                DELETED_LEAGUE_ID, Round.SEMI_FINAL)).isTrue();
        assertThat(bracketMatchRepository.existsByLeagueIdAndRoundAndGameIsNotNull(
                DELETED_LEAGUE_ID, Round.FINAL)).isFalse();
    }
}
