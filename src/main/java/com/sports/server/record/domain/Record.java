package com.sports.server.record.domain;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import com.sports.server.game.domain.GameTeamPlayer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @ManyToOne
    @JoinColumn(name = "game_team_player_id")
    private GameTeamPlayer gameTeamPlayer;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "scored_quarter", nullable = false)
    private String scoredQuarter;

    @Column(name = "scored_at", nullable = false)
    private LocalDateTime scoredAt;
}
