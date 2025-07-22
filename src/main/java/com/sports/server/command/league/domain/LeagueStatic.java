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
public class LeagueStatic extends BaseEntity<LeagueStatic> {

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

    public LeagueStatic(League league) {
        this.league = league;
        league.setLeagueStatic(this);
    }

    public LeagueStatic(League league, Team firstWinnerTeam, Team secondWinnerTeam,
                        Team mostCheeredTeam, Team mostCheerTalksTeam) {
        this.league = league;
        if (league.getLeagueStatic() != this) {
            league.setLeagueStatic(this);
        }

        if (firstWinnerTeam != null) {
            updateFirstWinnerTeam(firstWinnerTeam);
        }

        if (secondWinnerTeam != null) {
            updateSecondWinnerTeam(secondWinnerTeam);
        }

        if (mostCheeredTeam != null) {
            updateMostCheeredTeam(mostCheeredTeam);
        }

        if (mostCheerTalksTeam != null) {
            updateMostCheerTalksTeam(mostCheerTalksTeam);
        }
    }

    public void updateFirstWinnerTeam(Team firstWinnerTeam) {
        if (this.firstWinnerTeam != null) {
            this.firstWinnerTeam.removeFirstWinLeagueStatic(this);
        }

        this.firstWinnerTeam = firstWinnerTeam;
        if (firstWinnerTeam != null) {
            firstWinnerTeam.addFirstWinLeagueStatic(this);
        }
    }

    public void updateSecondWinnerTeam(Team secondWinnerTeam) {
        if (this.secondWinnerTeam != null) {
            this.secondWinnerTeam.removeSecondWinLeagueStatic(this);
        }

        this.secondWinnerTeam = secondWinnerTeam;
        if (secondWinnerTeam != null) {
            secondWinnerTeam.addSecondWinLeagueStatic(this);
        }
    }

    public void updateMostCheeredTeam(Team mostCheeredTeam) {
        if (this.mostCheeredTeam != null) {
            this.mostCheeredTeam.removeMostCheeredLeagueStatic(this);
        }

        this.mostCheeredTeam = mostCheeredTeam;
        if (mostCheeredTeam != null) {
            mostCheeredTeam.addMostCheeredLeagueStatic(this);
        }
    }

    public void updateMostCheerTalksTeam(Team mostCheerTalksTeam) {
        if (this.mostCheerTalksTeam != null) {
            this.mostCheerTalksTeam.removeMostCheerTalksLeagueStatic(this);
        }

        this.mostCheerTalksTeam = mostCheerTalksTeam;
        if (mostCheerTalksTeam != null) {
            mostCheerTalksTeam.addMostCheerTalksLeagueStatic(this);
        }
    }
}