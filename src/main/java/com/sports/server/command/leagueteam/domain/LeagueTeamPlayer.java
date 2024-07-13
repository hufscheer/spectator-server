package com.sports.server.command.leagueteam.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "league_team_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTeamPlayer extends BaseEntity<LeagueTeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id", nullable = false)
    private LeagueTeam leagueTeam;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "number", nullable = true)
    private int number;

    public LeagueTeamPlayer(LeagueTeam leagueTeam, String name, int number) {
        this.leagueTeam = leagueTeam;
        this.name = name;
        this.number = number;
    }
}
