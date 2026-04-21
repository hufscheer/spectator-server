package com.sports.server.command.team.domain;

import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "units")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Unit extends BaseEntity<Unit> {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Unit(String name, Organization organization) {
        this.name = name;
        this.organization = organization;
    }
}