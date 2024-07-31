package com.sports.server.command.league.domain;

import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.common.exception.CustomException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Round {
    FINAL("결승"),
    SEMI_FINAL("4강"),
    QUARTER_FINAL("8강"),
    ROUND_16("16강"),
    ROUND_32("32강");

    private final String description;

    public static Round from(final String value) {
        return Stream.of(Round.values())
                .collect(Collectors.toMap(Round::getDescription, round -> round))
                .computeIfAbsent(value, v -> {
                    throw new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.ROUND_NOT_FOUND_EXCEPTION);
                });
    }
}
