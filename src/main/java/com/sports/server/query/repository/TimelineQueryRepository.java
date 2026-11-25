package com.sports.server.query.repository;

import com.sports.server.command.team.domain.PlayerGoalCount;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.query.dto.PlayerTeamGoalCount;
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

    @Query("SELECT st FROM ScoreTimeline st JOIN FETCH st.scorer sc JOIN FETCH sc.gameTeam WHERE st.game.id = :gameId")
    List<ScoreTimeline> findScoreTimelinesByGameId(@Param("gameId") Long gameId);

    @Query("SELECT ogt FROM OwnGoalTimeline ogt JOIN FETCH ogt.scorer sc JOIN FETCH sc.gameTeam WHERE ogt.game.id = :gameId")
    List<OwnGoalTimeline> findOwnGoalTimelinesByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COALESCE(SUM(st.score), 0) FROM ScoreTimeline st WHERE st.scorer.player.id = :playerId")
    int countTotalGoalsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCount(st.scorer.player.id, SUM(st.score)) " +
            "FROM ScoreTimeline st " +
            "WHERE st.scorer.player.id IN :playerIds " +
            "GROUP BY st.scorer.player.id")
    List<PlayerGoalCount> countTotalGoalsByPlayerId(@Param("playerIds") List<Long> playerIds);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCount(st.scorer.player.id, SUM(st.score)) " +
            "FROM ScoreTimeline st " +
            "WHERE st.scorer.player.id IN :playerIds " +
            "AND fk(st.scorer.gameTeam.team) = :teamId " +
            "GROUP BY st.scorer.player.id")
    List<PlayerGoalCount> countTotalGoalsByPlayerIdInTeam(@Param("playerIds") List<Long> playerIds,
                                                          @Param("teamId") Long teamId);

    @Query("""
            SELECT new com.sports.server.query.dto.TeamTopScorerResult(
                cast(fk(sc.gameTeam.team) as long),
                new com.sports.server.command.team.domain.PlayerGoalCountWithRank(
                    p.id, p.studentNumber, p.name, SUM(st.score),
                    RANK() OVER (PARTITION BY fk(sc.gameTeam.team) ORDER BY SUM(st.score) DESC)
                )
            )
            FROM ScoreTimeline st
            JOIN st.scorer sc
            JOIN sc.player p
            WHERE fk(sc.gameTeam.team) IN :teamIds
            GROUP BY fk(sc.gameTeam.team), p.id, p.studentNumber, p.name
            HAVING SUM(st.score) > 0
            ORDER BY fk(sc.gameTeam.team), SUM(st.score) DESC, p.name ASC
            """)
    List<TeamTopScorerResult> findTopScorersByTeamIds(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT new com.sports.server.command.team.domain.PlayerGoalCountWithRank(" +
            "       p.id, " +
            "       p.studentNumber, " +
            "       p.name, " +
            "       SUM(st.score), " +
            "       RANK() OVER (ORDER BY SUM(st.score) DESC)) " +
            "FROM ScoreTimeline st " +
            "JOIN st.scorer sc " +
            "JOIN sc.player p " +
            "JOIN st.game g " +
            "WHERE fk(g.league) = :leagueId " +
            "AND g.state = com.sports.server.command.game.domain.GameState.FINISHED " +
            "GROUP BY p.id, p.studentNumber, p.name " +
            "HAVING SUM(st.score) > 0 " +
            "ORDER BY SUM(st.score) DESC, p.name ASC")
    List<PlayerGoalCountWithRank> findTopScorersByLeagueId(@Param("leagueId") Long leagueId, Pageable pageable);

    @Query("SELECT new com.sports.server.query.dto.PlayerTeamGoalCount(" +
            "       sc.player.id, cast(fk(sc.gameTeam.team) as long), SUM(st.score), MAX(st.id)) " +
            "FROM ScoreTimeline st " +
            "JOIN st.scorer sc " +
            "JOIN st.game g " +
            "WHERE fk(g.league) = :leagueId " +
            "AND g.state = com.sports.server.command.game.domain.GameState.FINISHED " +
            "AND sc.player.id IN :playerIds " +
            "GROUP BY sc.player.id, fk(sc.gameTeam.team)")
    List<PlayerTeamGoalCount> countLeagueGoalsByPlayerAndTeam(@Param("leagueId") Long leagueId,
                                                              @Param("playerIds") List<Long> playerIds);

}
