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

import static com.sports.server.report.exception.ReportErrorMessage.NOT_FOUND_COMMENT;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportFactory {

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
