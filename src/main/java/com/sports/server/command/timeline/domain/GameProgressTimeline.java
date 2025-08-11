package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@DiscriminatorValue("GAME_PROGRESS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameProgressTimeline extends Timeline {

    private static final String NAME_OF_BEFORE_GAME_QUARTER = "경기전";

    @Enumerated(EnumType.STRING)
    @Column(name = "game_progress_type")
    private GameProgressType gameProgressType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_quarter")
    private Quarter previousQuarter;

    @Column(name = "previous_quarter_changed_at")
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

        if (gameProgressType == GameProgressType.QUARTER_START && previousQuarter.getName()
                .equals(NAME_OF_BEFORE_GAME_QUARTER)) {
            game.updateState(GameState.SCHEDULED);
        }

        if (gameProgressType == GameProgressType.GAME_END) {
            game.updateState(GameState.PLAYING);
        }
    }
}
