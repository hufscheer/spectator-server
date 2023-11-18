package com.sports.server.comment.presentation;

import com.sports.server.comment.application.CommentService;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<Void> register(@RequestBody @Valid final CommentRequestDto commentRequestDto) {
        commentService.register(commentRequestDto);
        return ResponseEntity.ok(null);
    }

    @GetMapping("games/{gameId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getAllComments(@PathVariable final Long gameId) {
        return ResponseEntity.ok(commentService.getAllCommentsWithGameId(gameId));
    }
}
