package com.sports.server.command.leagueteam.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "team_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPlayer extends BaseEntity<TeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    public TeamPlayer(Team team, Player player) {
        this.team = team;
        this.player = player;
    }
}