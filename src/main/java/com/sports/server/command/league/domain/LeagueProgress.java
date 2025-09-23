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
            (today, league) -> today.isBefore(league.getStartAt())), 2),
    IN_PROGRESS("진행 중", (
            (today, league) -> (today.isEqual(league.getStartAt()) || today.isAfter(league.getStartAt()))
                    && (today.isBefore(league.getEndAt()))), 1),
    FINISHED("종료", (
            (today, league) -> (today.isEqual(league.getEndAt()) || today.isAfter(league.getEndAt()))), 3);

    private final String description;
    private final BiFunction<LocalDateTime, League, Boolean> InProgressFunction;
    private final int order;

    public static LeagueProgress fromDate(final LocalDateTime localDateTime, final League league) {
        return Stream.of(LeagueProgress.values())
                .filter(lp -> lp.getInProgressFunction().apply(localDateTime, league))
                .findFirst()
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.PROGRESS_NOT_FOUND_EXCEPTION));
    }
}
