package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("REPLACEMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplacementTimeline extends Timeline {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_lineup_player_id")
    private LineupPlayer originLineupPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_lineup_player_id")
    private LineupPlayer replacedLineupPlayer;

    @Override
    public String getType() {
        return "REPLACEMENT";
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

        originLineupPlayer.deactivatePlayerInGame();
        replacedLineupPlayer.activatePlayerInGame();

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

    }

    @Override
    public void rollback() {
        this.originLineupPlayer.activatePlayerInGame();
        this.replacedLineupPlayer.deactivatePlayerInGame();
    }
}
