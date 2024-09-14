package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.sport.domain.Quarter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("GAME_PROGRESS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameProgressTimeline extends Timeline {

    @Enumerated(EnumType.STRING)
    @Column(name = "game_progress_type")
    private GameProgressType gameProgressType;

    public GameProgressTimeline(
            Game game,
            Quarter quarter,
            Integer recordedAt,
            GameProgressType gameProgressType
    ) {
        super(game, quarter, recordedAt);
        this.gameProgressType = gameProgressType;
    }

    @Override
    public TimelineType getType() {
        return TimelineType.GAME_PROGRESS;
    }

    @Override
    public void apply() {
        // TODO Game 상태 변경
    }

    @Override
    public void rollback() {
        // TODO 게임 상태 변경 타임라인 생성 로직 구현 이후 구현 예정
    }
}
