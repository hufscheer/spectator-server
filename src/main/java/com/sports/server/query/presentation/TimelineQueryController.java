package com.sports.server.query.presentation;

import com.sports.server.query.application.TimelineQueryService;
import com.sports.server.query.dto.response.TimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimelineQueryController {

    private final TimelineQueryService timelineQueryService;

    @GetMapping("/games/{gameId}/timeline")
    public ResponseEntity<List<TimelineResponse>> getTimeline(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getTimeline(gameId));
    }
}
