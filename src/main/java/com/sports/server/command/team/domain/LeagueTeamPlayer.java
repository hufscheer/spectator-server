package com.sports.server.command.team.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
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
}
