package com.sports.server.command.timeline;

import com.sports.server.command.timeline.domain.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimelineFixtureRepository extends JpaRepository<Timeline, Long> {
    @Query("""
            SELECT t
                FROM Timeline t
                WHERE t.game.id = :gameId
                ORDER BY t.id DESC
            """)
    List<Timeline> findAllLatest(Long gameId);
}
