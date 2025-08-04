package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "league_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueStatistics extends BaseEntity<LeagueStatistics> {

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_winner_team_id")
    private Team firstWinnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_winner_team_id")
    private Team secondWinnerTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "most_cheered_team_id")
    private Team mostCheeredTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "most_cheer_talks_team_id")
    private Team mostCheerTalksTeam;

    public LeagueStatistics(League league) {
        this.league = league;
    }

    public void updateFirstWinnerTeam(Team firstWinnerTeam) {
        this.firstWinnerTeam = firstWinnerTeam;
    }

    public void updateSecondWinnerTeam(Team secondWinnerTeam) {
        this.secondWinnerTeam = secondWinnerTeam;
    }

    public void updateMostCheeredTeam(Team mostCheeredTeam) {
        this.mostCheeredTeam = mostCheeredTeam;
    }

    public void updateMostCheerTalksTeam(Team mostCheerTalksTeam) {
        this.mostCheerTalksTeam = mostCheerTalksTeam;
    }
}