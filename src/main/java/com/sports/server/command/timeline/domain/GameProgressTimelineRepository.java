package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameProgressTimelineRepository extends Repository<GameProgressTimeline, Long> {
    Optional<GameProgressTimeline> findFirstByGameOrderByIdDesc(Game game);

    @Query("SELECT gpt FROM GameProgressTimeline gpt WHERE gpt.game.id = :gameId AND gpt.gameProgressType = :type ORDER BY gpt.id ASC")
    List<GameProgressTimeline> findByGameIdAndType(@Param("gameId") Long gameId, @Param("type") GameProgressType type);
}