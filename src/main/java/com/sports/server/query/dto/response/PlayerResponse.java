package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.player.domain.Player;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerResponse(
	Long playerId,
	String name,
	String studentNumber,
	Integer jerseyNumber,
	int totalGoalCount,
	List<TeamResponse> teams
) {
	public PlayerResponse(final Player player) {
		this(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null,
				0,
				Collections.emptyList()
		);
	}

	public static PlayerResponse of(final Player player, final int totalGoalCount, final List<TeamResponse> teams) {
		return new PlayerResponse(
				player.getId(),
				player.getName(),
				player.getStudentNumber(),
				null,
				totalGoalCount,
				teams
		);
	}
}
