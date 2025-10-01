package com.sports.server.command.timeline.mapper;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.dto.TimelineRequest;
import java.util.List;

@FunctionalInterface
public interface TimelineSupplier {
    Timeline get(Game game, List<GameTeam> gameTeams, TimelineRequest request);
}
