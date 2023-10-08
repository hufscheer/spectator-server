package com.sports.server.comment.dto.request;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.ValidatorMessages;
import com.sports.server.game.domain.Game;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = ValidatorMessages.CONTENT_OF_COMMENT_CANNOT_BE_BLANK)
    private String content;

    @NotNull(message = ValidatorMessages.GAME_ID_CANNOT_BE_BLANK)
    private Long gameId;

    public Comment toEntity(final Game game) {
        return Comment.builder()
                .createdAt(LocalDateTime.now())
                .content(content)
                .game(game)
                .build();
    }
}
