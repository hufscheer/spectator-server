package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "timelines")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Timeline extends BaseEntity<Timeline> {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "game_id", nullable = false)
    protected Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "recorded_quarter", nullable = false)
    protected Quarter recordedQuarter;

    @Column(name = "recorded_at", nullable = false)
    protected Integer recordedAt;

    public abstract TimelineType getType();

    protected Timeline(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt
    ) {
        validateRecordedAt(recordedAt);

        this.game = game;
        this.recordedQuarter = recordedQuarter;
        this.recordedAt = recordedAt;
    }

    private void validateRecordedAt(Integer recordedAt) {
        if (recordedAt < 0) {
            throw new BadRequestException(ExceptionMessages.INVALID_RECORDED_AT);
        }
    }

    public abstract void apply();

    public abstract void rollback();
}
