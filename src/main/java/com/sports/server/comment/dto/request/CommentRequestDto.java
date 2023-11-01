package com.sports.server.comment.dto.request;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.ValidatorMessages;
import com.sports.server.game.domain.GameTeam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = ValidatorMessages.CONTENT_OF_COMMENT_CANNOT_BE_BLANK)
    private String content;

    @NotNull(message = ValidatorMessages.TEAM_ID_CANNOT_BE_BLANK)
    private Long gameTeamId;

    public Comment toEntity(final GameTeam team) {
        return Comment.builder()
                .createdAt(LocalDateTime.now())
                .content(content)
                .gameTeam(team)
                .game(team.getGame())
                .isBlocked(false)
                .build();
    }
}
