package com.sports.server.comment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "comments")
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

    @Column(name = "game_team_id", nullable = false)
    private Long gameTeamId;

    public boolean isBlocked() {
        return isBlocked;
    }

    public Comment(final String content, final Long gameTeamId) {
        this.createdAt = LocalDateTime.now();
        this.content = content;
        this.isBlocked = false;
        this.gameTeamId = gameTeamId;
    }
}
