package com.sports.server.query.repository;

import com.sports.server.command.timeline.domain.Timeline;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface TimelineQueryRepository extends Repository<Timeline, Long> {

    @Query("select t from Timeline t " +
            "join fetch t.game g " +
            "join fetch t.recordedQuarter rq " +
            "where t.game.id = :gameId " +
            "order by t.recordedAt desc, t.id desc")
    List<Timeline> findByGameId(Long gameId);

    @Query("SELECT count(st) FROM ScoreTimeline st WHERE st.scorer.id = :playerId")
    int countTotalGoalsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT st.scorer.player.id AS playerId, COUNT(st.id) AS goalCount " +
            "FROM ScoreTimeline st " +
            "WHERE st.scorer.player.id IN :playerIds " +
            "GROUP BY st.scorer.player.id")
    List<Object[]> countTotalGoalsByPlayerId(@Param("playerIds") List<Long> playerIds);
}
