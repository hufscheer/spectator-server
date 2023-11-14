package com.sports.server.report.domain;

import com.sports.server.comment.domain.Comment;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ReportRepository extends Repository<Report, Long> {

    void save(Report report);

    Optional<Report> findByComment(Comment comment);
}
