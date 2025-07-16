package com.sports.server.command.team.domain;

import com.sports.server.command.player.domain.Player;
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
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    public TeamPlayer(Team team, Player player) {
        this.team = team;
        this.player = player;
        
        // 양방향 매핑을 위한 로직
        team.addTeamPlayer(this);
        player.addTeamPlayer(this);
    }

    public void changeTeam(Team newTeam) {
        if (this.team != null) {
            this.team.removeTeamPlayer(this);
        }

        this.team = newTeam;
        if (newTeam != null) {
            newTeam.addTeamPlayer(this);
        }
    }

    public void changePlayer(Player newPlayer) {
        if (this.player != null) {
            this.player.removeTeamPlayer(this);
        }
        this.player = newPlayer;
        if (newPlayer != null) {
            newPlayer.addTeamPlayer(this);
        }
    }
}