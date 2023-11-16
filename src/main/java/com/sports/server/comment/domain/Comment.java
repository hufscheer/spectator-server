package com.sports.server.comment.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity<Comment> {

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
        registerEvent(new CommentEvent(this));
    }
}
