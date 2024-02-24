package com.sports.server.query.application;

import com.sports.server.command.league.domain.League;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InProgressLeagueChecker {

    public static boolean check(LocalDate today, League league) {
        LocalDate startAt = league.getStartAt().toLocalDate();
        LocalDate endAt = league.getEndAt().toLocalDate();
        return (today.isEqual(startAt) || today.isAfter(startAt))
                && (today.isBefore(endAt)) || today.isEqual(endAt);
    }
}
