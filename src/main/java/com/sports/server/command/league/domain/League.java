package com.sports.server.command.league.domain;

import com.sports.server.command.league.exception.LeagueErrorMessages;
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

    @Column(name = "third_place_match_enabled", nullable = false)
    private boolean thirdPlaceMatchEnabled;

    /**
     * 대진표를 쓰는 대회인지. {@code null} 은 아직 정하지 않았다는 뜻이다.
     * <p>
     * 대진표 데이터 유무로는 이 값을 대신할 수 없다. "대진표를 쓸 건데 아직 안 만든 대회" 와
     * "리그전이라 대진표가 없는 대회" 가 둘 다 행이 없는 상태로 똑같이 보이기 때문이다.
     */
    @Column(name = "bracket_enabled")
    private Boolean bracketEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType;

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
            final Round maxRound,
            final SportType sportType,
            final boolean thirdPlaceMatchEnabled,
            final Boolean bracketEnabled
    ) {
        this.administrator = administrator;
        this.organization = organization;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.maxRound = maxRound;
        this.inProgressRound = maxRound;
        this.sportType = sportType != null ? sportType : SportType.SOCCER;
        this.thirdPlaceMatchEnabled = thirdPlaceMatchEnabled;
        this.bracketEnabled = bracketEnabled;
        this.isDeleted = false;
    }

    public void updateInfo(String name, LocalDateTime startAt, LocalDateTime endAt, Round maxRound,
                           boolean thirdPlaceMatchEnabled, Boolean bracketEnabled) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
        this.startAt = startAt;
        this.endAt = endAt;
        this.maxRound = maxRound;
        this.thirdPlaceMatchEnabled = thirdPlaceMatchEnabled;
        this.bracketEnabled = bracketEnabled;
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

    public void validateRound(int roundNumber, boolean thirdPlaceMatch) {
        if (thirdPlaceMatch) {
            validateThirdPlaceMatchEnabled();
            return;
        }
        validateRoundWithinLimit(roundNumber);
    }

    private void validateThirdPlaceMatchEnabled() {
        if (!thirdPlaceMatchEnabled) {
            throw new BadRequestException(LeagueErrorMessages.THIRD_PLACE_NOT_ENABLED);
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
