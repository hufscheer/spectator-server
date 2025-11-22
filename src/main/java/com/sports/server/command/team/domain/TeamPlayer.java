package com.sports.server.command.team.domain;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "team_players",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uc_team_player",
                columnNames = {"team_id", "player_id"}
        )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPlayer extends BaseEntity<TeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    private TeamPlayer(Team team, Player player, Integer jerseyNumber) {
        this.team = team;
        this.player = player;
        this.jerseyNumber = jerseyNumber;
    }

    public static TeamPlayer of(Team team, Player player, Integer jerseyNumber) {
        TeamPlayer teamPlayer = new TeamPlayer(team, player, jerseyNumber);
        team.addTeamPlayer(teamPlayer);
        player.addTeamPlayer(teamPlayer);
        return teamPlayer;
    }

}