package com.sports.server.report;

import com.sports.server.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportFixtureRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByCommentId(Long commentId);
}
