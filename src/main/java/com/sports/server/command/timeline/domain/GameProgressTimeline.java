package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("GAME_PROGRESS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameProgressTimeline extends Timeline {

    @Enumerated(EnumType.STRING)
    @Column(name = "game_progress_type")
    private GameProgressType gameProgressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_quarter_id")
    private Quarter previousQuarter;

    private LocalDateTime previousQuarterChangedAt;

    public GameProgressTimeline(
            Game game,
            Quarter quarter,
            Integer recordedAt,
            GameProgressType gameProgressType
    ) {
        super(game, quarter, recordedAt);
        this.gameProgressType = gameProgressType;
        this.previousQuarter = game.getQuarter();
        this.previousQuarterChangedAt = game.getQuarterChangedAt();

        validateQuarter();
    }

    private void validateQuarter() {
        if (recordedQuarter.isPreviousThan(previousQuarter)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이전 쿼터로의 진행은 불가능합니다.");
        }
    }

    @Override
    public TimelineType getType() {
        return TimelineType.GAME_PROGRESS;
    }

    @Override
    public void apply() {
        if (gameProgressType == GameProgressType.GAME_START) {
            game.play();
        }

        if (gameProgressType == GameProgressType.QUARTER_START) {
            game.updateQuarter(recordedQuarter);
        }

        if (gameProgressType == GameProgressType.GAME_END) {
            game.end();
        }
    }

    @Override
    public void rollback() {
        game.updateQuarter(previousQuarter, previousQuarterChangedAt);

        if (gameProgressType == GameProgressType.GAME_START) {
            game.updateState(GameState.SCHEDULED);
        }

        if (gameProgressType == GameProgressType.GAME_END) {
            game.updateState(GameState.PLAYING);
        }
    }
}
