package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "NewLeagueTeam")
@Getter
@Table(name = "league_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTeam extends BaseEntity<LeagueTeam> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "total_cheer_count")
    private Integer totalCheerCount;

    @Column(name = "total_talk_count")
    private Integer totalTalkCount;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "team_color")
    private String teamColor;

    public LeagueTeam(League league, Team team, String teamColor) {
        this.league = league;
        this.team = team;
        this.teamColor = teamColor;
        this.totalCheerCount = 0;
        this.totalTalkCount = 0;
        this.ranking = 0;
        
        // 양방향 매핑을 위한 로직
        league.addLeagueTeam(this);
        team.addLeagueTeam(this);
    }

    public void updateTotalCheerCount(Integer totalCheerCount) {
        this.totalCheerCount = totalCheerCount;
    }

    public void updateTotalTalkCount(Integer totalTalkCount) {
        this.totalTalkCount = totalTalkCount;
    }

    public void updateRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public void updateTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

    public void incrementCheerCount() {
        this.totalCheerCount++;
    }

    public void incrementTalkCount() {
        this.totalTalkCount++;
    }

}