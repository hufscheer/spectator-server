package com.sports.server.command.timeline;

import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimelineFixtureRepository extends JpaRepository<Timeline, Long> {
    @Query("""
            SELECT t
                FROM Timeline t
                WHERE t.game.id = :gameId
                ORDER BY t.id DESC
            """)
    List<Timeline> findAllLatest(Long gameId);

    @Query("""
                SELECT rt 
                FROM ReplacementTimeline rt
                LEFT JOIN FETCH rt.originLineupPlayer olp
                LEFT JOIN FETCH rt.replacedLineupPlayer rlp
                WHERE rt.game.id = :gameId
                ORDER BY rt.id DESC
            """)
    List<ReplacementTimeline> findReplacementTimelineWithLineupPlayers(Long gameId);
}
