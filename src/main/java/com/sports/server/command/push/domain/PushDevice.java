package com.sports.server.command.push.domain;

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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushDevice extends BaseEntity<PushDevice> {

    private static final int FINGERPRINT_LENGTH = 8;

    @Column(name = "device_key", nullable = false, unique = true, updatable = false)
    private String deviceKey;

    @Column(name = "token", nullable = false)
    private String token;

    // 토큰은 대소문자를 구분하지만 MySQL 유니크 인덱스는 구분하지 않아서 해시로 유일성을 건다
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PushPlatform platform;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    public PushDevice(String token, PushPlatform platform) {
        this.deviceKey = UUID.randomUUID().toString();
        this.token = token;
        this.tokenHash = hashOf(token);
        this.platform = platform;
        this.createdAt = LocalDateTime.now();
        this.lastSeenAt = this.createdAt;
    }

    public static String hashOf(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public void touch(PushPlatform platform) {
        this.platform = platform;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void changeToken(String token) {
        this.token = token;
        this.tokenHash = hashOf(token);
        this.lastSeenAt = LocalDateTime.now();
    }

    // 로그에는 토큰 대신 이 값만 남긴다
    public String fingerprint() {
        return tokenHash.substring(0, FINGERPRINT_LENGTH);
    }
}
