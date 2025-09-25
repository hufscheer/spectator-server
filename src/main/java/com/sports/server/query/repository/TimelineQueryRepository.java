package com.sports.server.query.repository;

import com.sports.server.command.team.domain.PlayerGoalCount;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.query.dto.TeamTopScorerResult;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface TimelineQueryRepository extends Repository<Timeline, Long> {

    @Query("select t from Timeline t " +
            "join fetch t.game g " +
            "where t.game.id = :gameId " +
            "order by t.recordedAt desc, t.id desc")
    List<Timeline> findByGameId(Long gameId);

    @Query("SELECT count(st) FROM ScoreTimeline st WHERE st.scorer.id = :playerId")
    int countTotalGoalsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCount(st.scorer.player.id, COUNT(st.id)) " +
            "FROM ScoreTimeline st " +
            "WHERE st.scorer.player.id IN :playerIds " +
            "GROUP BY st.scorer.player.id")
    List<PlayerGoalCount> countTotalGoalsByPlayerId(@Param("playerIds") List<Long> playerIds);

    @Query("""
            SELECT new com.sports.server.query.dto.TeamTopScorerResult(
                tp.team.id,
                new com.sports.server.command.team.domain.PlayerGoalCountWithRank(
                    p.id, p.studentNumber, p.name, COUNT(st.id),
                    RANK() OVER (PARTITION BY tp.team.id ORDER BY COUNT(st.id) DESC)
                )
            )
            FROM ScoreTimeline st
            JOIN st.scorer sc
            JOIN sc.player p
            JOIN p.teamPlayers tp
            WHERE tp.team.id IN :teamIds
            GROUP BY tp.team.id, p.id, p.studentNumber, p.name
            HAVING COUNT(st.id) > 0
            ORDER BY tp.team.id, COUNT(st.id) DESC, p.name ASC
            """)
    List<TeamTopScorerResult> findTopScorersByTeamIds(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCountWithRank(" +
            "       p.id, " +
            "       p.studentNumber, " +
            "       p.name, " +
            "       COUNT(st.id), " +
            "       RANK() OVER (ORDER BY COUNT(st.id) DESC)) " +
            "FROM ScoreTimeline st " +
            "JOIN st.scorer sc " +
            "JOIN sc.player p " +
            "JOIN st.game g " +
            "WHERE g.league.id = :leagueId " +
            "GROUP BY p.id, p.studentNumber, p.name " +
            "HAVING COUNT(st.id) > 0 " +
            "ORDER BY COUNT(st.id) DESC, p.name ASC")
    List<PlayerGoalCountWithRank> findTopScorersByLeagueId(@Param("leagueId") Long leagueId, Pageable pageable);

}
