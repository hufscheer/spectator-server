package com.sports.server.command.report;

import com.sports.server.command.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportFixtureRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByCommentId(Long commentId);
}
