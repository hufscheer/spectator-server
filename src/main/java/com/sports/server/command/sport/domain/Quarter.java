package com.sports.server.command.sport.domain;

import com.sports.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quarters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Quarter extends BaseEntity<Quarter> {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sports_id")
    private Sport sport;
}
