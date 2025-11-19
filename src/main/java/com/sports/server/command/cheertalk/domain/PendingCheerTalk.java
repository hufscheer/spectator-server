package com.sports.server.command.cheertalk.domain;


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
@Table(name = "pending_cheer_talks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingCheerTalk extends BaseEntity<PendingCheerTalk> {

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PendingCheerTalk(String destination, CheerTalk cheerTalk) {
        this.destination = destination;
        this.payload = cheerTalk.getContent();
        this.createdAt = LocalDateTime.now();
    }
}
