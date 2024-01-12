package com.sports.server.command.leagueteam;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.command.league.domain.League;
import jakarta.persistence.*;
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

}
