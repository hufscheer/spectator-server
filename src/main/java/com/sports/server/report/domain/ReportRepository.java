package com.sports.server.report.domain;

import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Report, Long> {

    void save(Report report);
}
