package com.sports.server.command.league.domain;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@Table(name = "league_team_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTeamPlayer extends BaseEntity<LeagueTeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id", nullable = false)
    private LeagueTeam leagueTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "jersey_number", nullable = true)
    private int jerseyNumber;

    private LeagueTeamPlayer(@NonNull LeagueTeam leagueTeam, @NonNull Player player) {
        this.leagueTeam = leagueTeam;
        this.player = player;
    }

    public static LeagueTeamPlayer of(@NonNull LeagueTeam leagueTeam, @NonNull Player player) {
        LeagueTeamPlayer leagueTeamPlayer = new LeagueTeamPlayer(leagueTeam, player);
        leagueTeam.addLeaguePlayer(leagueTeamPlayer);
        return leagueTeamPlayer;
    }

    public void changeJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }
}
