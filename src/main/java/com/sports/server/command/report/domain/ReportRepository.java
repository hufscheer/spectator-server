package com.sports.server.command.report.domain;

import com.sports.server.command.comment.domain.CheerTalk;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Report, Long> {

    void save(Report report);

    Optional<Report> findByCheerTalk(CheerTalk cheerTalk);
}
