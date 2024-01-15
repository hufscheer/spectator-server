package com.sports.server.command.report;

import com.sports.server.command.report.domain.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportFixtureRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByCheerTalkId(Long cheerTalkId);
}
