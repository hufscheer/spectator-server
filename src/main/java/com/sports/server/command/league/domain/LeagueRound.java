package com.sports.server.command.league.domain;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.common.exception.CustomException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueRound {
	FINAL("결승"),
	SEMI_FINAL("4강"),
	QUARTER_FINAL("8강"),
	ROUND_16("16강"),
	ROUND_32("32강");

	private final String description;

	public static LeagueRound from(final String value) {
		return Stream.of(LeagueRound.values())
			.collect(Collectors.toMap(LeagueRound::getDescription, round -> round))
			.computeIfAbsent(value, v -> {
				throw new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.ROUND_NOT_FOUND_EXCEPTION);
			});
	}
}
