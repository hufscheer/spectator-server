package com.sports.server.command.comment.application;

import static com.sports.server.command.comment.exception.CommentErrorMessages.COMMENT_CONTAINS_BAD_WORD;

import com.sports.server.command.comment.domain.Comment;
import com.sports.server.command.comment.domain.CommentDynamicRepository;
import com.sports.server.command.comment.domain.CommentRepository;
import com.sports.server.command.comment.domain.LanguageFilter;
import com.sports.server.command.comment.dto.request.CommentRequestDto;
import com.sports.server.command.comment.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.common.exception.CustomException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Comment> comments = commentDynamicRepository.findByGameIdOrderByStartTime(
                gameId, pageRequest.cursor(), pageRequest.size()
        );

        List<Long> gameTeamIds = getOrderedGameTeamIds(comments);

        List<CommentResponse> responses = comments.stream()
                .map(comment -> {
                    return new CommentResponse(comment, getOrderOfGameTeamId(comment.getGameTeamId(), gameTeamIds));
                })
                .collect(Collectors.toList());

        Collections.reverse(responses);
        return responses;
    }

    private int getOrderOfGameTeamId(final Long gameTeamId, final List<Long> gameTeamIds) {
        return gameTeamIds.indexOf(gameTeamId) + 1;
    }

    private List<Long> getOrderedGameTeamIds(final List<Comment> comments) {
        return comments.stream()
                .map(Comment::getGameTeamId).collect(Collectors.toSet()).stream().sorted().toList();
    }

}
