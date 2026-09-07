package com.sports.server.command.game.dto;

import com.sports.server.command.game.domain.Position;

public record LineupPlayerPositionUpdateRequest(
		Position position
) {
}
