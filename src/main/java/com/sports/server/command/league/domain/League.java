package com.sports.server.command.league.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.Where;
import org.springframework.util.StringUtils;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "leagues")
@Where(clause = "is_deleted = 0")
@SQLDelete(sql = "UPDATE leagues SET is_deleted = 1 WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class League extends BaseEntity<League> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member manager;

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

    public League(
            final Member manager,
            final Organization organization,
            final String name,
            final LocalDateTime startAt,
            final LocalDateTime endAt,
            final Round maxRound
    ) {
        this.manager = manager;
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

	public boolean isManagedBy(Member manager) {
		return this.manager.equals(manager);
	}

    public void delete() {
        this.isDeleted = true;
    }

    public String manager() {
        return manager.getEmail();
    }
}
