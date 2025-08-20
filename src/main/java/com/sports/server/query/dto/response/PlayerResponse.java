package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.TeamPlayer;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerResponse(
	Long playerId,
	Long teamPlayerId,
	String name,
	String studentNumber,
	Integer jerseyNumber,
	Integer totalGoalCount,
	List<TeamResponse> teams
) {
	public PlayerResponse(final Player player, final Long teamPlayerId) {
		this(
				player.getId(),
				teamPlayerId,
				player.getName(),
				player.getStudentNumber(),
				null,
				0,
				Collections.emptyList()
		);
	}

	public static PlayerResponse of(final Player player, final Long teamPlayerId, final int totalGoalCount, final List<TeamResponse> teams) {
		return new PlayerResponse(
				player.getId(),
				teamPlayerId,
				player.getName(),
				player.getStudentNumber(),
				null,
				totalGoalCount,
				teams
		);
	}

	public static PlayerResponse of(final TeamPlayer teamPlayer, final int totalGoalCount) {
		Player player = teamPlayer.getPlayer();
		return new PlayerResponse(
				player.getId(),
				teamPlayer.getId(),
				player.getName(),
				player.getStudentNumber(),
				teamPlayer.getJerseyNumber(),
				totalGoalCount,
				null
		);
	}
}
