package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueStatisticsQueryRepository extends Repository<LeagueStatistics, Long> {

    LeagueStatistics findByLeagueId(Long leagueId);

    @Query("SELECT ls FROM LeagueStatistics ls JOIN FETCH ls.league " +
            "WHERE ls.firstWinnerTeam.id = :teamId OR ls.secondWinnerTeam.id = :teamId")
    List<LeagueStatistics> findTrophiesByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT ls.league.id, ls.firstWinnerTeam.name " +
            "FROM LeagueStatistics ls " +
            "WHERE ls.league.id IN :leagueIds AND ls.firstWinnerTeam IS NOT NULL")
    List<Object[]> findWinnerTeamNamesByLeagueIds(@Param("leagueIds") List<Long> leagueIds);
}
