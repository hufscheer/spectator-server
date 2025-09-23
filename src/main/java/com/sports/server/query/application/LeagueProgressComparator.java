package com.sports.server.query.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

import java.time.LocalDateTime;
import java.util.Comparator;

public class LeagueProgressComparator implements Comparator<League> {

    private final LocalDateTime baseTime;

    public LeagueProgressComparator(LocalDateTime baseTime) {
        this.baseTime = baseTime;
    }

    @Override
    public int compare(League o1, League o2) {
        LeagueProgress p1 = LeagueProgress.fromDate(baseTime, o1);
        LeagueProgress p2 = LeagueProgress.fromDate(baseTime, o2);
        return Integer.compare(p1.getOrder(), p2.getOrder());
    }
}
