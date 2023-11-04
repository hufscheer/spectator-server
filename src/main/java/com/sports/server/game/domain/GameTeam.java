package com.sports.server.game.domain;

import com.sports.server.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "game_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "cheer_count", nullable = false)
    private int cheerCount;

    @Column(name = "score", nullable = false)
    private int score;
}
