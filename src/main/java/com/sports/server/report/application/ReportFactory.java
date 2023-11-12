package com.sports.server.report.application;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.report.domain.Report;
import com.sports.server.report.domain.ReportRepository;
import com.sports.server.report.dto.request.ReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFactory {

    private static final String NOT_FOUND_COMMENT = "존재하지 않는 댓글입니다.";

    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;

    public Report create(ReportRequest request) {
        Comment comment = getCommentById(request.commentId());
        return reportRepository.findByComment(comment)
                .orElse(new Report(comment));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_COMMENT));
    }
}
