package com.sports.server.command.league.domain;

import com.sports.server.command.team.domain.Team;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "LeagueTeam")
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

    @OneToMany(mappedBy = "leagueTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTeamPlayer> leagueTeamPlayers = new ArrayList<>();

    @Column(name = "total_cheer_count")
    private int totalCheerCount;

    @Column(name = "total_talk_count")
    private int totalTalkCount;

    @Column(name = "ranking")
    private int ranking;

    private LeagueTeam(League league,Team team) {
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

    public void addLeaguePlayer(LeagueTeamPlayer leaguePlayer) {
        if (!this.leagueTeamPlayers.contains(leaguePlayer)) {
            this.leagueTeamPlayers.add(leaguePlayer);
        }
    }

    public void removeLeagueTeamPlayer(LeagueTeamPlayer leagueTeamPlayer) {
        this.leagueTeamPlayers.remove(leagueTeamPlayer);
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