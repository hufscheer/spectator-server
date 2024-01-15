package com.sports.server.command.game.domain;

import com.sports.server.command.leagueteam.LeagueTeam;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "game_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTeam extends BaseEntity<GameTeam> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_team_id")
    private LeagueTeam leagueTeam;

    @Column(name = "cheer_count", nullable = false)
    private int cheerCount;

    @Column(name = "score", nullable = false)
    private int score;

    public boolean matchGame(final Game game) {
        return this.game.equals(game);
    }
}
