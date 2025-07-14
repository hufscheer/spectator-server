package com.sports.server.command.newEntity.team.domain;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.newEntity.league.domain.LeagueStatic;
import com.sports.server.command.newEntity.league.domain.LeagueTeam;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity<Team> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_image_url", nullable = false)
    private String logoImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private TeamUnit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Member administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "firstWinnerTeam")
    private List<LeagueStatic> firstWinLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "secondWinnerTeam")
    private List<LeagueStatic> secondWinLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "mostCheeredTeam")
    private List<LeagueStatic> mostCheeredLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "mostCheerTalksTeam")
    private List<LeagueStatic> mostCheerTalksLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTeam> leagueTeams = new ArrayList<>();

    public Team(String name, String logoImageUrl, TeamUnit unit, Member administrator, Organization organization) {
        this.name = name;
        this.logoImageUrl = logoImageUrl;
        this.unit = unit;
        this.administrator = administrator;
        this.organization = organization;
    }

    public void updateInfo(String name, String logoImageUrl, TeamUnit unit) {
        this.name = name;
        this.logoImageUrl = logoImageUrl;
        this.unit = unit;
    }

    public void changeAdministrator(Member newAdministrator) {
        this.administrator = newAdministrator;
    }

    public void changeOrganization(Organization newOrganization) {
        this.organization = newOrganization;
    }

    public void addTeamPlayer(TeamPlayer teamPlayer) {
        this.teamPlayers.add(teamPlayer);
    }

    public void removeTeamPlayer(TeamPlayer teamPlayer) {
        this.teamPlayers.remove(teamPlayer);
    }

    public void addFirstWinLeagueStatic(LeagueStatic leagueStatic) {
        this.firstWinLeagueStatics.add(leagueStatic);
    }

    public void removeFirstWinLeagueStatic(LeagueStatic leagueStatic) {
        this.firstWinLeagueStatics.remove(leagueStatic);
    }

    public void addSecondWinLeagueStatic(LeagueStatic leagueStatic) {
        this.secondWinLeagueStatics.add(leagueStatic);
    }

    public void removeSecondWinLeagueStatic(LeagueStatic leagueStatic) {
        this.secondWinLeagueStatics.remove(leagueStatic);
    }

    public void addMostCheeredLeagueStatic(LeagueStatic leagueStatic) {
        this.mostCheeredLeagueStatics.add(leagueStatic);
    }

    public void removeMostCheeredLeagueStatic(LeagueStatic leagueStatic) {
        this.mostCheeredLeagueStatics.remove(leagueStatic);
    }

    public void addMostCheerTalksLeagueStatic(LeagueStatic leagueStatic) {
        this.mostCheerTalksLeagueStatics.add(leagueStatic);
    }

    public void removeMostCheerTalksLeagueStatic(LeagueStatic leagueStatic) {
        this.mostCheerTalksLeagueStatics.remove(leagueStatic);
    }

    public void addLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.add(leagueTeam);
    }

    public void removeLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.remove(leagueTeam);
    }
}