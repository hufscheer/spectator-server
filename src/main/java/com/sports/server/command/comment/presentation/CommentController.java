package com.sports.server.command.comment.presentation;

import com.sports.server.command.comment.application.CommentService;
import com.sports.server.command.comment.dto.request.CommentRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/comments")
    public ResponseEntity<Void> register(@RequestBody @Valid final CommentRequestDto commentRequestDto) {
        commentService.register(commentRequestDto);
        return ResponseEntity.ok(null);
    }
}
