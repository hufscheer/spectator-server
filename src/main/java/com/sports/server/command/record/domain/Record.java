package com.sports.server.command.record.domain;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record extends BaseEntity<Record> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_team_id")
    private GameTeam gameTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lineup_player_id")
    private LineupPlayer lineupPlayer;

    @Column(name = "score", nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_quarter_id")
    private Quarter recordedQuarter;

    @Column(name = "recorded_at", nullable = false)
    private Integer recordedAt;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;
}
