package com.sports.server.command.cheertalk.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "cheer_talks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheerTalk extends BaseEntity<CheerTalk> {

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_status", nullable = false)
    private CheerTalkBlockStatus blockStatus;

    @Column(name = "game_team_id", nullable = false)
    private Long gameTeamId;

    public boolean isBlocked() {
        return blockStatus != CheerTalkBlockStatus.ACTIVE;
    }

    public void blockByAdmin() {
        this.blockStatus = CheerTalkBlockStatus.BLOCKED_BY_ADMIN;
    }

    public void blockByBot() {
        this.blockStatus = CheerTalkBlockStatus.BLOCKED_BY_BOT;
    }

    public void unblock() {
        this.blockStatus = CheerTalkBlockStatus.ACTIVE;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public CheerTalk(final String content, final Long gameTeamId) {
        this.createdAt = LocalDateTime.now();
        this.content = content;
        this.blockStatus = CheerTalkBlockStatus.ACTIVE;
        this.gameTeamId = gameTeamId;
    }
}
