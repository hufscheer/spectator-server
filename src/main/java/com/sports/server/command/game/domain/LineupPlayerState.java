package com.sports.server.command.game.domain;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

import com.sports.server.command.game.exception.LineupErrorMessages;
import com.sports.server.common.exception.CustomException;

public enum LineupPlayerState {
	STARTER, CANDIDATE;

	public static LineupPlayerState from(final String value) {
		return Arrays.stream(LineupPlayerState.values())
			.filter(state -> state.name().equalsIgnoreCase(value))
			.findFirst().orElseThrow(
				() -> new CustomException(HttpStatus.BAD_REQUEST, LineupErrorMessages.STATE_NOT_FOUND_EXCEPTION)
			);
	}
}
