package com.sports.server.command.comment.application;

import com.sports.server.command.comment.domain.Comment;
import com.sports.server.command.comment.domain.CommentRepository;
import com.sports.server.command.comment.domain.LanguageFilter;
import com.sports.server.command.comment.dto.request.CommentRequestDto;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sports.server.command.comment.exception.CommentErrorMessages.COMMENT_CONTAINS_BAD_WORD;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final LanguageFilter languageFilter;

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
}
