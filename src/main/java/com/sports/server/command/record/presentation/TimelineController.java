package com.sports.server.command.record.presentation;

import com.sports.server.command.record.application.TimelineService;
import com.sports.server.command.record.dto.response.TimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/games/{gameId}/timeline")
    public ResponseEntity<List<TimelineResponse>> getTimeline(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineService.getTimeline(gameId));
    }
}
