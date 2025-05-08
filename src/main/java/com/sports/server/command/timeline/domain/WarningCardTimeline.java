package com.sports.server.command.timeline.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue("WARNING_CARD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarningCardTimeline extends Timeline{

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scorer_id")
    private LineupPlayer scorer;

    @Enumerated(EnumType.STRING)
    @Column(name = "warning_card_type")
    private WarningCardType warningCardType;

    public WarningCardTimeline(Game game,
                               Quarter recordedQuarter,
                               Integer recordedAt,
                               LineupPlayer scorer,
                               WarningCardType warningCardType
    ) {
        super(game, recordedQuarter, recordedAt);
        this.scorer = scorer;
        this.warningCardType = warningCardType;
    }

    @Override
    public TimelineType getType() {
        return TimelineType.WARNING_CARD;
    }

    @Override
    public void apply() {
        game.issueWarningCard(scorer);
    }

    @Override
    public void rollback() {
        game.cancelWarningCard(scorer);
    }
}
