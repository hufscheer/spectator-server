package com.sports.server.command.league.dto;

import java.time.LocalDateTime;

public record LeagueRequest(
	Long organizationId,
	String name,
	Integer maxRound,
	LocalDateTime startAt,
	LocalDateTime endAt
) {
}
