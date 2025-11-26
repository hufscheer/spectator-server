package com.sports.server.command.league.domain;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.domain.ManagedEntity;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.flywaydb.core.internal.util.StringUtils;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "leagues")
@Where(clause = "is_deleted = 0")
@SQLDelete(sql = "UPDATE leagues SET is_deleted = 1 WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class League extends BaseEntity<League> implements ManagedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Member administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "max_round")
    private Round maxRound;

    @Column(name = "in_progress_round")
    private Round inProgressRound;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LeagueTeam> leagueTeams = new ArrayList<>();

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTopScorer> topScorers = new ArrayList<>();

    public League(
            final Member administrator,
            final Organization organization,
            final String name,
            final LocalDateTime startAt,
            final LocalDateTime endAt,
            final Round maxRound
    ) {
        this.administrator = administrator;
        this.organization = organization;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.maxRound = maxRound;
        this.inProgressRound = maxRound;
        this.isDeleted = false;
    }

    public void updateInfo(String name, LocalDateTime startAt, LocalDateTime endAt, Round maxRound) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
        this.startAt = startAt;
        this.endAt = endAt;
        this.maxRound = maxRound;
    }

    @Override
    public boolean isManagedBy(Member manager) {
        return manager.getId() == 1 || this.administrator.equals(manager);
    }

    public void delete() {
        this.isDeleted = true;
    }

    public String manager() {
        return administrator.getEmail();
    }

    public void validateRoundWithinLimit(Integer round) {
        if (maxRound.numberIsLessThan(round)) {
            throw new BadRequestException(ExceptionMessages.LEAGUE_ROUND_EXCEEDS_MAX);
        }
    }

    public void addTopScorer(LeagueTopScorer topScorer) {
        this.topScorers.add(topScorer);
    }

    public void removeTopScorer(LeagueTopScorer topScorer) {
        this.topScorers.remove(topScorer);
    }

    public void addLeagueTeam(LeagueTeam leagueTeam) {
        if (!this.leagueTeams.contains(leagueTeam)) {
            this.leagueTeams.add(leagueTeam);
        }
    }

    public void removeLeagueTeam(LeagueTeam leagueTeam) {
        this.leagueTeams.remove(leagueTeam);
        if (leagueTeam.getTeam() != null) {
            leagueTeam.getTeam().removeLeagueTeam(leagueTeam);
        }
    }
}
