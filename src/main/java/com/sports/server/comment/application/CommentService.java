package com.sports.server.comment.application;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.domain.CommentDynamicRepository;
import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.comment.domain.LanguageFilter;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.sports.server.comment.exception.CommentErrorMessages.COMMENT_CONTAINS_BAD_WORD;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentDynamicRepository commentDynamicRepository;
    private final LanguageFilter languageFilter;

    @Transactional
    public void register(final CommentRequestDto commentRequestDto) {
        validateContent(commentRequestDto.content());
        Comment comment = new Comment(commentRequestDto.content(), commentRequestDto.gameTeamId());
        commentRepository.save(comment);
    }

    private void validateContent(final String content) {
        if (languageFilter.containsBadWord(content)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, COMMENT_CONTAINS_BAD_WORD);
        }
    }

    public List<CommentResponse> getCommentsByGameId(final Long gameId, final PageRequestDto pageRequest) {
        List<CommentResponse> responses = commentDynamicRepository.findByGameIdOrderByStartTime(
                        gameId, pageRequest.cursor(), pageRequest.size()
                )
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        Collections.reverse(responses);
        return responses;
    }
}
