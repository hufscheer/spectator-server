package com.sports.server.command.cheertalk.domain;


import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Convert;
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

    private String destination;

    @Convert(converter = CheerTalkConverter.class)
    private CheerTalk cheerTalk;

    private LocalDateTime createdAt;

    public PendingCheerTalk(String destination, CheerTalk cheerTalk) {
        this.destination = destination;
        this.cheerTalk = cheerTalk;
        this.createdAt = LocalDateTime.now();
    }
}
