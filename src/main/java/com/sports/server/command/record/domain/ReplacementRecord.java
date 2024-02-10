package com.sports.server.command.record.domain;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "replacement_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplacementRecord extends BaseEntity<ReplacementRecord> {

    @OneToOne
    @JoinColumn(name = "record_id")
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_lineup_player_id")
    private LineupPlayer originLineupPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_lineup_player_id")
    private LineupPlayer replacedLineupPlayer;
}
