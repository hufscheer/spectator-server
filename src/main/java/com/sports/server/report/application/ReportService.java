package com.sports.server.report.application;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.report.domain.Report;
import com.sports.server.report.domain.ReportRepository;
import com.sports.server.report.dto.request.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;

    public void report(final ReportRequest request) {
        Comment comment = commentRepository.findById(request.commentId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
        reportRepository.save(new Report(comment));
    }
}
