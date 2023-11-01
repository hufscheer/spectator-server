package com.sports.server.comment.domain;

import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @ManyToOne
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    public boolean isBlocked() {
        return isBlocked;
    }

    public Comment(final String content, final GameTeam gameTeam, final Game game) {
        this.createdAt = LocalDateTime.now();
        this.content = content;
        this.isBlocked = false;
        this.gameTeam = gameTeam;
        this.game = game;
    }
}
