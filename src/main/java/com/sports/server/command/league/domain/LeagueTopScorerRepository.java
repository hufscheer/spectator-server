package com.sports.server.command.league.domain;

import com.sports.server.query.dto.PlayerGoalSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueTopScorerRepository extends Repository<LeagueTopScorer, Long> {
    LeagueTopScorer save(LeagueTopScorer leagueTopScorer);
    
    void delete(LeagueTopScorer leagueTopScorer);
    
    List<LeagueTopScorer> findByLeagueId(Long leagueId);
    
    void deleteByLeagueId(Long leagueId);
    
    @Query("SELECT new com.sports.server.query.dto.PlayerGoalSummary(lts.player, SUM(lts.goalCount)) " +
           "FROM LeagueTopScorer lts " +
           "WHERE YEAR(lts.league.startAt) = :year " +
           "GROUP BY lts.player " +
           "ORDER BY SUM(lts.goalCount) DESC")
    List<PlayerGoalSummary> findTop5PlayersByYearWithTotalGoals(@Param("year") Integer year, Pageable pageable);
}