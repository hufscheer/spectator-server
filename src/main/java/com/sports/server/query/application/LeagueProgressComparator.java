package com.sports.server.query.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;

import java.time.LocalDateTime;
import java.util.Comparator;

public class LeagueProgressComparator implements Comparator<League> {

    @Override
    public int compare(League o1, League o2) {

        LeagueProgress leagueProgressOfO1 = LeagueProgress.fromDate(LocalDateTime.now(), o1);
        LeagueProgress leagueProgressOfO2 = LeagueProgress.fromDate(LocalDateTime.now(), o2);

        return leagueProgressOfO1.getOrder() - leagueProgressOfO2.getOrder();
    }
}
