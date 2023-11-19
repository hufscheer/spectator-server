package com.sports.server.team.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "team_players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPlayer extends BaseEntity<TeamPlayer> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;
}
