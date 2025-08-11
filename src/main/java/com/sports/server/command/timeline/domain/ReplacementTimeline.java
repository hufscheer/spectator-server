package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("REPLACEMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplacementTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "origin_lineup_player_id")
    private LineupPlayer originLineupPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "replaced_lineup_player_id")
    private LineupPlayer replacedLineupPlayer;

    @Override
    public TimelineType getType() {
        return TimelineType.REPLACEMENT;
    }

    public ReplacementTimeline(
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
            throw new IllegalArgumentException("다른 팀의 선수끼리 교체할 수 없습니다.");
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
