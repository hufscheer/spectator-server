package com.sports.server.command.bracket.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "bracket_matches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BracketMatch extends BaseEntity<BracketMatch> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(name = "round", nullable = false)
    private Round round;

    @Column(name = "match_number", nullable = false)
    private int matchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id")
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id")
    private Team team2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    public BracketMatch(final League league, final Round round, final int matchNumber) {
        this.league = league;
        this.round = round;
        this.matchNumber = matchNumber;
    }

    public void placeTeams(final Team team1, final Team team2) {
        this.team1 = team1;
        this.team2 = team2;
    }

    public void linkGame(final Game game) {
        this.game = game;
    }

    public void unlinkGame() {
        this.game = null;
    }

    public boolean isLinked() {
        return game != null;
    }

    public void removeTeam(final Team team) {
        if (team1 != null && team1.equals(team)) {
            team1 = null;
        }
        if (team2 != null && team2.equals(team)) {
            team2 = null;
        }
    }
}
