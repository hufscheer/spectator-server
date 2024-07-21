package com.sports.server.command.timeline.presentation;

import com.sports.server.command.timeline.TimelineDto;
import com.sports.server.command.timeline.application.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/timelines")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;

    @PostMapping("/score")
    public ResponseEntity<Void> createScoreTimeline(@PathVariable Long gameId,
                                                    @RequestBody TimelineDto.RegisterScore request) {
        timelineService.registerScoreTimeline(gameId, request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .build();
    }
}
