package com.sports.server.record.domain;

import com.sports.server.game.domain.Game;
import com.sports.server.team.domain.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "score", nullable = false)
    private int score;
}
