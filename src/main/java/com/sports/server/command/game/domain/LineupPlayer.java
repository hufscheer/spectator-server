package com.sports.server.command.game.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "lineup_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineupPlayer extends BaseEntity<LineupPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;
}
