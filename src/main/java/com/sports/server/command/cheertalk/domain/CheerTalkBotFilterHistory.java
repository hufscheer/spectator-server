package com.sports.server.command.cheertalk.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "cheer_talk_bot_filter_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheerTalkBotFilterHistory extends BaseEntity<CheerTalk> {

    @ManyToOne(optional = true)
    @JoinColumn(name = "cheer_talk_id", nullable = true)
    private CheerTalk cheerTalk;

    private LocalDateTime filteredAt;

    @Enumerated(EnumType.STRING)
    private CheerTalkFilterResult cheerTalkFilterResult;

    private BotType botType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private JsonNode rawBotResponse;

    private Integer latencyMs;
}
