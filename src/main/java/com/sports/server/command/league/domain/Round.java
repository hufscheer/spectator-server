package com.sports.server.command.league.domain;

import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.common.exception.BadRequestException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sports.server.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum Round {
    // number 는 참가 팀 수. 3·4위전도 두 팀이 겨루지만 브래킷 트리 밖이라 inBracketTree 로 구분한다.
    FINAL("결승", 2, true),
    THIRD_PLACE_MATCH("3·4위전", 2, false),
    SEMI_FINAL("4강", 4, true),
    QUARTER_FINAL("8강", 8, true),
    ROUND_16("16강", 16, true),
    PRELIMINARY("예선", 100, true);

    private final String description;
    private final int number;
    private final boolean inBracketTree;

    public static Round from(final String value) {
        return Stream.of(Round.values())
                .collect(Collectors.toMap(Round::getDescription, round -> round))
                .computeIfAbsent(value, v -> {
                    throw new BadRequestException(LeagueErrorMessages.ROUND_NOT_FOUND_EXCEPTION);
                });
    }

    public static Round from(int number) {
        return Stream.of(Round.values())
                .filter(Round::isInBracketTree)
                .filter(round -> round.number == number)
                .findAny()
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST,
                        LeagueErrorMessages.ROUND_NOT_FOUND_EXCEPTION));
    }


    public static boolean isValidDescription(final String value) {
        return Stream.of(Round.values())
                .anyMatch(round -> round.getDescription().equals(value));
    }

    public static boolean isValidNumber(final Integer value) {
        if (value == null) {
            return false;
        }
        return Stream.of(Round.values())
                .filter(Round::isInBracketTree)
                .anyMatch(round -> round.getNumber() == value);
    }

    public boolean numberIsLessThan(Integer otherNumber) {
        return this.number < otherNumber;
    }
}
