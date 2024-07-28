package com.sports.server.command.league.domain;

import java.time.LocalDateTime;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.common.exception.CustomException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeagueProgress {
	BEFORE_START("시작 전", (
		(today, league) -> today.isBefore(league.getStartAt()))),
	IN_PROGRESS("진행 중", (
		(today, league) -> (today.isEqual(league.getStartAt()) || today.isAfter(league.getStartAt()))
			&& (today.isBefore(league.getEndAt())))),
	FINISHED("종료", (
		(today, league) -> (today.isEqual(league.getEndAt()) || today.isAfter(league.getEndAt()))));

	private final String description;
	private final BiFunction<LocalDateTime, League, Boolean> InProgressFunction;

	public static String check(final LocalDateTime localDateTime, final League league) {
		return Stream.of(LeagueProgress.values())
			.filter(lp -> lp.getInProgressFunction().apply(localDateTime, league))
			.map(LeagueProgress::getDescription)
			.findFirst()
			.orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.PROGRESS_NOT_FOUND_EXCEPTION));
	}
}
