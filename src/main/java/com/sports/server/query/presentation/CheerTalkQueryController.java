package com.sports.server.query.presentation;

import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.query.application.CheerTalkQueryService;
import com.sports.server.query.dto.response.CheerTalkResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheerTalkQueryController {

    private final CheerTalkQueryService cheerTalkQueryService;

    @GetMapping("games/{gameId}/cheer-talks")
    public ResponseEntity<List<CheerTalkResponse>> getAllCheerTalks(@PathVariable final Long gameId,
                                                                    @ModelAttribute final PageRequestDto pageRequest) {

        return ResponseEntity.ok(cheerTalkQueryService.getCheerTalksByGameId(gameId, pageRequest));
    }
}
