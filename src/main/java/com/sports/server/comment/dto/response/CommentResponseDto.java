package com.sports.server.comment.dto.response;

import com.sports.server.comment.domain.Comment;
import com.sports.server.common.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private String content;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT)
    private LocalDateTime createdAt;

    public CommentResponseDto(final Comment comment) {
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}
