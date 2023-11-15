package com.sports.server.comment.application;

import static com.sports.server.comment.exception.CommentErrorMessages.COMMENT_CONTAINS_BAD_WORD;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponseDto;
import com.sports.server.common.exception.CustomException;
import com.vane.badwordfiltering.BadWordFiltering;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BadWordFiltering badWordFiltering;

    @Transactional
    public void register(final CommentRequestDto commentRequestDto) {
        validateContent(commentRequestDto.content());
        Comment comment = new Comment(commentRequestDto.content(), commentRequestDto.gameTeamId());
        commentRepository.save(comment);
    }

    private void validateContent(final String content) {
        if (badWordFiltering.blankCheck(content)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, COMMENT_CONTAINS_BAD_WORD);
        }
    }

    public List<CommentResponseDto> getAllCommentsWithGameId(final Long gameId) {
        return commentRepository.getAllByGameOrderByCreatedAtDesc(gameId)
                .stream()
                .map(CommentResponseDto::new)
                .toList();
    }
}
