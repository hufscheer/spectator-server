package com.sports.server.comment.dto.response;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.Constants;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
public class CommentResponseDto {

    private Long id;

    private String content;

    private boolean isBlocked;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT)
    private LocalDateTime createdAt;

    public boolean getIsBlocked() {
        return isBlocked;
    }

    public CommentResponseDto(final Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.isBlocked = comment.isBlocked();
        this.createdAt = comment.getCreatedAt();
    }
}
