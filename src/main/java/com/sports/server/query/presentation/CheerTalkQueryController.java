package com.sports.server.query.presentation;

import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.CheerTalkQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CheerTalkQueryController {

    private final CheerTalkQueryService cheerTalkQueryService;

    @GetMapping("games/{gameId}/cheer-talks")
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable final Long gameId,
                                                                @ModelAttribute final PageRequestDto pageRequest) {

        return ResponseEntity.ok(cheerTalkQueryService.getCommentsByGameId(gameId, pageRequest));
    }
}
