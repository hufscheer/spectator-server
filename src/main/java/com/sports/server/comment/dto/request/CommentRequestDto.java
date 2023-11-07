package com.sports.server.comment.dto.request;

import com.sports.server.comment.domain.Comment;
import com.sports.server.comment.dto.CommentValidatorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = CommentValidatorMessages.CONTENT_OF_COMMENT_CANNOT_BE_BLANK)
    private String content;

    @NotNull(message = CommentValidatorMessages.TEAM_ID_CANNOT_BE_BLANK)
    private Long gameTeamId;
}
