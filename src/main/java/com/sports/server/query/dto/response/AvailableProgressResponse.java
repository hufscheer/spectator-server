package com.sports.server.query.dto.response;

import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.timeline.domain.GameProgressType;

import java.util.List;

public record AvailableProgressResponse(List<ProgressAction> availableActions) {

    public record ProgressAction(
            String quarter,
            GameProgressType gameProgressType,
            String displayName
    ) {
        public static ProgressAction of(Quarter quarter, GameProgressType type) {
            String displayName = type == GameProgressType.GAME_END
                    ? type.getDisplayName()
                    : quarter.getDisplayName() + " " + type.getDisplayName();
            return new ProgressAction(quarter.name(), type, displayName);
        }
    }
}
