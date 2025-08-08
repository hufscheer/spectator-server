package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.player.domain.Player;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerResponse(
	Long playerId,
	String name,
	String studentNumber,
	Integer jerseyNumber,
	int totalGoalCount
) {
	public PlayerResponse(final Player player) {
		this(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null,
				0
		);
	}

	public static PlayerResponse of(final Player player, final int totalGoalCount) {
		return new PlayerResponse(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null,
				totalGoalCount
		);
	}
}
