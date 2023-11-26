package com.sports.server.comment.presentation;

import com.sports.server.comment.application.CommentService;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable final Long gameId,
                                                                @ModelAttribute final PageRequestDto pageRequest) {

        return ResponseEntity.ok(commentService.getCommentsByGameId(gameId, pageRequest));
    }
}
