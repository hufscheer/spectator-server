package com.sports.server.comment.presentation;

import com.sports.server.comment.application.CommentService;
import com.sports.server.comment.dto.CommentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid final CommentDto commentDto) {
        commentService.register(commentDto);
        return ResponseEntity.ok(null);
    }
}
