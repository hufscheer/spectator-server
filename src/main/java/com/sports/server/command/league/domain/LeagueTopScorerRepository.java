package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueTopScorerRepository extends Repository<LeagueTopScorer, Long> {
    LeagueTopScorer save(LeagueTopScorer leagueTopScorer);

    void delete(LeagueTopScorer leagueTopScorer);

    List<LeagueTopScorer> findByLeagueId(Long leagueId);

    // 파생 삭제는 행마다 조회 후 지워서, 다른 트랜잭션이 먼저 지운 행이면 StaleStateException 이 난다
    @Modifying
    @Query("DELETE FROM LeagueTopScorer lts WHERE lts.league.id = :leagueId")
    void deleteAllByLeagueId(@Param("leagueId") Long leagueId);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCountWithRank(" +
            "lts.player.id, lts.player.studentNumber, lts.player.name, SUM(lts.goalCount), " +
            "ROW_NUMBER() OVER (ORDER BY SUM(lts.goalCount) DESC)) " +
            "FROM LeagueTopScorer lts " +
            "WHERE YEAR(lts.league.startAt) = :year " +
            "AND (:organizationId IS NULL OR lts.league.organization.id = :organizationId) " +
            "AND (:sportType IS NULL OR lts.league.sportType = :sportType) " +
            "GROUP BY lts.player " +
            "ORDER BY SUM(lts.goalCount) DESC")
    List<PlayerGoalCountWithRank> findTopPlayersByYearWithTotalGoals(@Param("year") Integer year,
                                                                       @Param("organizationId") Long organizationId,
                                                                       @Param("sportType") SportType sportType,
                                                                       Pageable pageable);

    List<LeagueTopScorer> saveAll(Iterable<LeagueTopScorer> leagueTopScorers);
}