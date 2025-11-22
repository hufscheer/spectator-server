package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
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

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCountWithRank(" +
            "lts.player.id, lts.player.studentNumber, lts.player.name, SUM(lts.goalCount), " +
            "ROW_NUMBER() OVER (ORDER BY SUM(lts.goalCount) DESC)) " +
            "FROM LeagueTopScorer lts " +
            "WHERE YEAR(lts.league.startAt) = :year " +
            "GROUP BY lts.player " +
            "ORDER BY SUM(lts.goalCount) DESC")
    List<PlayerGoalCountWithRank> findTopPlayersByYearWithTotalGoals(@Param("year") Integer year, Pageable pageable);

    List<LeagueTopScorer> saveAll(Iterable<LeagueTopScorer> leagueTopScorers);
}