package com.sports.server.command.game.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record LineupPlayerStateUpdateRequest(
	@NotBlank
	String state,
	@NotEmpty
	List<Long> lineupPlayerIds
) {
}
