package com.sports.server.query.repository;

import com.sports.server.command.league.domain.LeagueStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueStatisticsQueryRepository extends Repository<LeagueStatistics, Long> {

    @Query("""
            SELECT ls FROM LeagueStatistics ls
            LEFT JOIN FETCH ls.firstWinnerTeam
            LEFT JOIN FETCH ls.secondWinnerTeam
            LEFT JOIN FETCH ls.mostCheeredTeam
            LEFT JOIN FETCH ls.mostCheerTalksTeam
            WHERE ls.league.id = :leagueId
            """)
    Optional<LeagueStatistics> findByLeagueId(@Param("leagueId") Long leagueId);

    @Query("SELECT ls FROM LeagueStatistics ls JOIN FETCH ls.league " +
            "WHERE ls.firstWinnerTeam.id IN :teamIds OR ls.secondWinnerTeam.id IN :teamIds")
    List<LeagueStatistics> findTrophiesByTeamIds(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT new com.sports.server.query.repository.LeagueWinnerInfo(ls.league.id, ls.firstWinnerTeam.name) " +
            "FROM LeagueStatistics ls " +
            "WHERE ls.league.id IN :leagueIds AND ls.firstWinnerTeam IS NOT NULL")
    List<LeagueWinnerInfo> findWinnerTeamInfoByLeagueIds(@Param("leagueIds") List<Long> leagueIds);
}
