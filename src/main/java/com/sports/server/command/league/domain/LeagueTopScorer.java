package com.sports.server.command.league.domain;

import com.sports.server.command.player.domain.Player;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "league_top_scorers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTopScorer extends BaseEntity<LeagueTopScorer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @Column(name = "goal_count", nullable = false)
    private Integer goalCount;

    public LeagueTopScorer(League league, Player player, Integer ranking, Integer goalCount) {
        this.league = league;
        this.player = player;
        this.ranking = ranking;
        this.goalCount = goalCount;

        league.addTopScorer(this);
        player.addLeagueTopScorer(this);
    }
}