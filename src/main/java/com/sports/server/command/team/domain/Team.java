package com.sports.server.command.team.domain;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueStatic;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.UnauthorizedException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity<Team> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Member administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_image_url", nullable = false)
    private String logoImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private Unit unit;

    @OneToMany(mappedBy = "leagueTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTeam> leagueTeams = new ArrayList<>();

    @OneToMany(mappedBy = "leagueTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameTeam> gameTeams = new ArrayList<>();

    @OneToMany(mappedBy = "firstWinnerTeam")
    private List<LeagueStatic> firstWinLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "secondWinnerTeam")
    private List<LeagueStatic> secondWinLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "mostCheeredTeam")
    private List<LeagueStatic> mostCheeredLeagueStatics = new ArrayList<>();

    @OneToMany(mappedBy = "mostCheerTalksTeam")
    private List<LeagueStatic> mostCheerTalksLeagueStatics = new ArrayList<>();

    public void addPlayer(Player player) {
        TeamPlayer teamPlayer = new TeamPlayer(this, player);
        this.teamPlayers.add(teamPlayer);
        player.getTeamPlayers().add(teamPlayer);
    }

    public void removePlayer(Player player) {
        TeamPlayer teamPlayer = findTeamPlayer(player);
        if (teamPlayer != null) {
            this.teamPlayers.remove(teamPlayer);
            player.getTeamPlayers().remove(teamPlayer);
        }
    }

    private TeamPlayer findTeamPlayer(Player player) {
        return this.teamPlayers.stream()
                .filter(tp -> tp.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

    public List<Player> getPlayers() {
        return this.teamPlayers.stream()
                .map(TeamPlayer::getPlayer)
                .collect(Collectors.toList());
    }


    public Team(String name, Unit unit, String logoImageUrl, Member administrator) {
        this.name = name;
        this.unit = unit;
        this.logoImageUrl = logoImageUrl;
        this.administrator = administrator;
        this.organization = administrator.getOrganization();
    }

    public void updateInfo(String name, String logoImageUrl, String originPrefix, String replacePrefix) {
        this.name = name;
        if (logoImageUrl != null) {
            if (!logoImageUrl.equals(this.logoImageUrl)) {
                this.logoImageUrl = changeLogoImageUrlToBeSaved(logoImageUrl, originPrefix, replacePrefix);
            }
        }
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl, String originPrefix, String replacePrefix) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }

    public void validateTeamPlayer(Player player) {
        TeamPlayer teamPlayer = findTeamPlayer(player);
        if (teamPlayer == null) {
            throw new IllegalStateException("해당 팀에 속하지 않은 선수입니다.");
        }
    }

    public void deleteLogoImageUrl() {
        this.logoImageUrl = "";
        registerEvent(new LogoImageDeletedEvent(logoImageUrl));
    }

    public void isParticipate(League league) {
        if (!participatesInLeague(league)) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public boolean participatesInLeague(League league) {
        return this.leagueTeams.stream()
                .anyMatch(leagueTeam -> leagueTeam.getLeague().equals(league));
    }

    public void addLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.add(leagueTeam);
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

}
