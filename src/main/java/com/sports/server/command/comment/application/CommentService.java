package com.sports.server.command.comment.application;

import static com.sports.server.command.comment.exception.CommentErrorMessages.COMMENT_CONTAINS_BAD_WORD;

import com.sports.server.command.comment.domain.CheerTalk;
import com.sports.server.command.comment.domain.CommentRepository;
import com.sports.server.command.comment.domain.LanguageFilter;
import com.sports.server.command.comment.dto.CommentRequestDto;
import com.sports.server.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final LanguageFilter languageFilter;

    public void register(final CommentRequestDto commentRequestDto) {
        validateContent(commentRequestDto.content());
        CheerTalk cheerTalk = new CheerTalk(commentRequestDto.content(), commentRequestDto.gameTeamId());
        commentRepository.save(cheerTalk);
    }

    private void validateContent(final String content) {
        if (languageFilter.containsBadWord(content)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, COMMENT_CONTAINS_BAD_WORD);
        }
    }
}
