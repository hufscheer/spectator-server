package com.sports.server.command.leagueteam.domain;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "league_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueTeam extends BaseEntity<LeagueTeam> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_image_url", nullable = false)
    private String logoImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @OneToMany(mappedBy = "leagueTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTeamPlayer> leagueTeamPlayers = new ArrayList<>();

    public void addPlayer(LeagueTeamPlayer leagueTeamPlayer) {
        leagueTeamPlayers.add(leagueTeamPlayer);
    }

    public LeagueTeam(String name, String logoImageUrl, Member manager, League league) {
        this.name = name;
        this.logoImageUrl = logoImageUrl;
        this.manager = manager;
        this.organization = manager.getOrganization();
        this.league = league;
    }

    public void updateInfo(String name, String logoImageUrl) {
        this.name = name;
        if (logoImageUrl != null) {
            this.logoImageUrl = logoImageUrl;
        }
    }

    public void validateLeagueTeamPlayer(LeagueTeamPlayer leagueTeamPlayer) {
        if (!this.leagueTeamPlayers.contains(leagueTeamPlayer)) {
            throw new IllegalStateException("해당 리그팀에 속하지 않은 선수입니다.");
        }
    }

    public void deleteLogoImageUrl() {
        this.logoImageUrl = "";
        registerEvent(new LogoImageDeletedEvent(logoImageUrl));
    }
}
