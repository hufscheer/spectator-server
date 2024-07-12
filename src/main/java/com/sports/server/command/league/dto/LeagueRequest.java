package com.sports.server.command.league.dto;

import java.time.LocalDateTime;

public record LeagueRequest(
	LocalDateTime startAt,
	LocalDateTime endAt,
	Integer maxRound,
	Long organizationId,
	String name
) {
}
