package com.sports.server.command.team.domain;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.player.domain.Player;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Entity
@Getter
@Table(name = "teams")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity<Team> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_image_url", nullable = false) // TODO:?
    private String logoImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = true)
    private Unit unit;

    @Column(name = "team_color", nullable = true)
    private String teamColor;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTeam> leagueTeams = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameTeam> gameTeams = new ArrayList<>();

    @Builder
    public Team(@NonNull String name, String logoImageUrl, Unit unit, String teamColor) {
        this.name = name;
        this.logoImageUrl = logoImageUrl;
        this.unit = unit;
        this.teamColor = teamColor;
    }

    public void update(String name, String logoImageUrl, String originPrefix, String replacePrefix, Unit unit, String teamColor) {
        this.name = name;
        this.unit = unit;
        this.teamColor = teamColor;
        if (logoImageUrl != null && !logoImageUrl.equals(this.logoImageUrl)) {
            this.logoImageUrl = changeLogoImageUrlToBeSaved(logoImageUrl, originPrefix, replacePrefix);
        }
    }

    public void addTeamPlayer(Player player) {
        boolean alreadyExists = this.teamPlayers.stream()
                .anyMatch(tp -> tp.getPlayer().equals(player));

        if (alreadyExists) {
            throw new IllegalStateException("이미 팀에 소속된 선수입니다.");
        }
        TeamPlayer teamPlayer = TeamPlayer.builder()
                .team(this)
                .player(player)
                .build();

        this.teamPlayers.add(teamPlayer);
        player.addTeamPlayer(teamPlayer);
    }

    public void removeTeamPlayer(Player player) {
        this.teamPlayers.stream()
                .filter(tp -> tp.getPlayer().equals(player))
                .findFirst()
                .ifPresent(teamPlayer -> {
                    this.teamPlayers.remove(teamPlayer);
                    player.getTeamPlayers().remove(teamPlayer);
                });
    }

    private String changeLogoImageUrlToBeSaved(String logoImageUrl, String originPrefix, String replacePrefix) {
        if (!logoImageUrl.contains(originPrefix)) {
            throw new IllegalStateException("잘못된 이미지 url 입니다.");
        }
        return logoImageUrl.replace(originPrefix, replacePrefix);
    }

    public void deleteLogoImageUrl() {
        this.logoImageUrl = "";
        registerEvent(new LogoImageDeletedEvent(logoImageUrl));
    }

    public void addLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.add(leagueTeam);
    }

    public void removeLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.remove(leagueTeam);
    }

    public void addGameTeam(GameTeam gameTeam) {
        this.gameTeams.add(gameTeam);
    }

    public void removeGameTeam(GameTeam gameTeam) {
        this.gameTeams.remove(gameTeam);
    }

}
