package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
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
    private int totalCheerCount;

    @Column(name = "total_talk_count")
    private int totalTalkCount;

    @Column(name = "ranking")
    private Integer ranking;

    private LeagueTeam(League league, Team team) {
        this.league = league;
        this.team = team;
        this.totalCheerCount = 0;
        this.totalTalkCount = 0;
        this.ranking = 0;
    }

    public static LeagueTeam of(League league, Team team) {
        LeagueTeam leagueTeam = new LeagueTeam(league, team);
        league.addLeagueTeam(leagueTeam);
        team.addLeagueTeam(leagueTeam);
        return leagueTeam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof LeagueTeam that)) return false;
        return Objects.equals(getLeague(), that.getLeague()) && Objects.equals(getTeam(), that.getTeam());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeague(), getTeam());
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

    public void incrementCheerCount() {
        this.totalCheerCount++;
    }

    public void incrementTalkCount() {
        this.totalTalkCount++;
    }

}