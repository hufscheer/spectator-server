package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.CommentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentQueryController {

    private final CommentQueryService commentQueryService;

    @GetMapping("games/{gameId}/comments")
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable final Long gameId,
                                                                @ModelAttribute final PageRequestDto pageRequest) {

        return ResponseEntity.ok(commentQueryService.getCommentsByGameId(gameId, pageRequest));
    }
}
