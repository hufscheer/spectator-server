package com.sports.server.query.presentation;

import com.sports.server.query.application.TimelineQueryService;
import com.sports.server.query.dto.response.AvailableProgressResponse;
import com.sports.server.query.dto.response.QuarterScoreResponse;
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
    public ResponseEntity<List<TimelineResponse>> getTimelines(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getTimelines(gameId));
    }

    @GetMapping("/games/{gameId}/available-progress")
    public ResponseEntity<AvailableProgressResponse> getAvailableProgress(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getAvailableProgress(gameId));
    }

    @GetMapping("/games/{gameId}/quarter-scores")
    public ResponseEntity<List<QuarterScoreResponse>> getQuarterScores(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getQuarterScores(gameId));
    }
}
