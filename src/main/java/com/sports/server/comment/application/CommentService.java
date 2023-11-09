package com.sports.server.comment.application;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public void register(final CommentRequestDto commentRequestDto) {
        Comment comment = new Comment(commentRequestDto.getContent(), commentRequestDto.getGameTeamId());
        commentRepository.save(comment);
    }

    public List<CommentResponseDto> getAllCommentsWithGameId(final Long gameId) {
        return commentRepository.getAllByGameOrderByCreatedAtDesc(gameId)
                .stream()
                .map(CommentResponseDto::new)
                .toList();
    }
}
