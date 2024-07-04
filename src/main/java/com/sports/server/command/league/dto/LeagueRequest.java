package com.sports.server.command.league.dto;

import java.time.LocalDateTime;

public record LeagueRequest(
	String name,
	LocalDateTime startAt,
	LocalDateTime endAt,
	Integer maxRound
) {
}
