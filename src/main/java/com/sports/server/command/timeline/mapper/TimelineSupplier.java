package com.sports.server.command.timeline.mapper;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.dto.TimelineRequest;

@FunctionalInterface
public interface TimelineSupplier {
    Timeline get(Game game, TimelineRequest request);
}
