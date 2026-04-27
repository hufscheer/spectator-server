package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ReplacementTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "origin_lineup_player_id")
    protected LineupPlayer originLineupPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "replaced_lineup_player_id")
    protected LineupPlayer replacedLineupPlayer;

    protected ReplacementTimeline(
            Game game,
            Quarter recordedQuarter,
            Integer recordedAt,
            LineupPlayer originLineupPlayer,
            LineupPlayer replacedLineupPlayer
    ) {
        super(game, recordedQuarter, recordedAt);
        validatePlayers(originLineupPlayer, replacedLineupPlayer);
        this.originLineupPlayer = originLineupPlayer;
        this.replacedLineupPlayer = replacedLineupPlayer;
    }

    private void validatePlayers(LineupPlayer originLineupPlayer, LineupPlayer replacedLineupPlayer) {
        if (!originLineupPlayer.isSameTeam(replacedLineupPlayer)) {
            throw new BadRequestException(ExceptionMessages.INVALID_PLAYER_SUBSTITUTION);
        }
        if (!originLineupPlayer.isPlaying()) {
            throw new BadRequestException(ExceptionMessages.REPLACEMENT_ORIGIN_NOT_IN_GAME);
        }
        if (replacedLineupPlayer.isPlaying()) {
            throw new BadRequestException(ExceptionMessages.REPLACEMENT_TARGET_ALREADY_IN_GAME);
        }
    }

    @Override
    public void apply() {
        this.originLineupPlayer.deactivatePlayerInGame();
        this.originLineupPlayer.recordReplacedPlayer(replacedLineupPlayer);
        this.replacedLineupPlayer.activatePlayerInGame();
        this.replacedLineupPlayer.recordReplacedPlayer(originLineupPlayer);
    }

    @Override
    public void rollback() {
        this.originLineupPlayer.activatePlayerInGame();
        this.originLineupPlayer.deleteReplacedPlayer();
        this.replacedLineupPlayer.deactivatePlayerInGame();
        this.replacedLineupPlayer.deleteReplacedPlayer();
    }

    public String getReplacedPlayerName() {
        if (replacedLineupPlayer == null) {
            return null;
        }
        return replacedLineupPlayer.getPlayer().getName();
    }
}
