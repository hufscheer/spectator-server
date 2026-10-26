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

    // 팀은 소프트 삭제라 연관으로 두면 삭제된 팀을 깨울 때 조회가 터진다. id 만 두고 읽을 때 따로 찾는다
    @Column(name = "team_id")
    private Long teamId;

    public LeagueTopScorer(League league, Player player, Integer ranking, Integer goalCount, Long teamId) {
        this.league = league;
        this.player = player;
        this.ranking = ranking;
        this.goalCount = goalCount;
        this.teamId = teamId;

        league.addTopScorer(this);
        player.addLeagueTopScorer(this);
    }
}