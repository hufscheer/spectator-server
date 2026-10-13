package com.sports.server.command.cheertalk.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
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

    @Column(name = "game_team_id")
    private Long gameTeamId;

    @Column(name = "is_ai_seed", nullable = false)
    private boolean isAiSeed;

    @Column(name = "author_key", length = 16)
    private String authorKey;

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
        this(content, gameTeamId, false);
    }

    public CheerTalk(final String content, final Long gameTeamId, final String visitorId) {
        this(content, gameTeamId, false);
        this.authorKey = toAuthorKey(visitorId);
    }

    public static CheerTalk createAiSeed(final String content, final Long gameTeamId) {
        return new CheerTalk(content, gameTeamId, true);
    }

    private CheerTalk(final String content, final Long gameTeamId, final boolean isAiSeed) {
        this.createdAt = LocalDateTime.now();
        this.content = content;
        this.blockStatus = CheerTalkBlockStatus.ACTIVE;
        this.gameTeamId = gameTeamId;
        this.isAiSeed = isAiSeed;
    }

    private static String toAuthorKey(final String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(visitorId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
