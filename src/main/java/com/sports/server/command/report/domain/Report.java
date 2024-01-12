package com.sports.server.command.report.domain;

import static com.sports.server.command.report.exception.ReportErrorMessage.INVALID_REPORT_BLOCKED_COMMENT;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "reports")
@Getter
public class Report extends BaseEntity<Report> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cheer_talk_id", unique = true)
    private CheerTalk cheerTalk;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportState state;

    protected Report() {
        registerEvent(new ReportEvent(this));
    }

    public Report(CheerTalk cheerTalk) {
        validateBlockedCheerTalk(cheerTalk);
        this.cheerTalk = cheerTalk;
        this.reportedAt = LocalDateTime.now();
        this.state = ReportState.UNCHECKED;
        registerEvent(new ReportEvent(this));
    }

    private void validateBlockedCheerTalk(CheerTalk cheerTalk) {
        if (cheerTalk.isBlocked()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, INVALID_REPORT_BLOCKED_COMMENT);
        }
    }

    public boolean isUnchecked() {
        return this.state == ReportState.UNCHECKED;
    }
}
