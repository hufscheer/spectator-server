package com.sports.server.league.domain;

import com.sports.server.member.domain.Member;
import com.sports.server.organization.domain.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leagues")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "administrator_id")
    private Member administrator;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;
}
