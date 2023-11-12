package com.sports.server.report.domain;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
public class Report extends BaseEntity<Report> {

    private static final String INVALID_REPORT_BLOCKED_COMMENT = "이미 블락된 댓글은 신고할 수 없습니다.";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", unique = true)
    private Comment comment;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportState state;

    protected Report() {
        registerEvent(new ReportEvent(this));
    }

    public Report(Comment comment) {
        validateBlockedComment(comment);
        this.comment = comment;
        this.reportedAt = LocalDateTime.now();
        this.state = ReportState.UNCHECKED;
        registerEvent(new ReportEvent(this));
    }

    private void validateBlockedComment(Comment comment) {
        if (comment.isBlocked()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, INVALID_REPORT_BLOCKED_COMMENT);
        }
    }

    public boolean isUnchecked() {
        return this.state == ReportState.UNCHECKED;
    }
}
