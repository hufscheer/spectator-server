package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "timelines")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Timeline extends BaseEntity<Timeline> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    protected Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_quarter_id", nullable = false)
    protected Quarter recordedQuarter;

    @Column(name = "recorded_at", nullable = false)
    protected Integer recordedAt;

    public abstract String getType();

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
            throw new IllegalArgumentException("시간은 0 이상이어야 합니다.");
        }
    }

    public abstract void apply();

    public abstract void rollback();
}
