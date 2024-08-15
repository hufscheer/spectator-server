package com.sports.server.command.timeline.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.application.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/games/{gameId}/timelines")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;

    @PostMapping("/score")
    public ResponseEntity<Void> createScoreTimeline(@PathVariable Long gameId,
                                                    @RequestBody TimelineRequest.RegisterScore request,
                                                    Member member) {
        timelineService.register(member, gameId, request);
        return ResponseEntity.created(URI.create("")).build();
    }

    @PostMapping("/replacement")
    public ResponseEntity<Void> createReplacementTimeline(@PathVariable Long gameId,
                                                          @RequestBody TimelineRequest.RegisterReplacement request,
                                                          Member member) {
        timelineService.register(member, gameId, request);
        return ResponseEntity.created(URI.create("")).build();
    }

    @PostMapping("/progress")
    public ResponseEntity<Void> createProgressTimeline(@PathVariable Long gameId,
                                                       @RequestBody TimelineRequest.RegisterProgress request,
                                                       Member member) {
        timelineService.register(member, gameId, request);
        return ResponseEntity.created(URI.create("")).build();
    }

    @DeleteMapping("/{timelineId}")
    public ResponseEntity<Void> deleteTimeline(@PathVariable Long gameId,
                                               @PathVariable Long timelineId,
                                               Member member) {
        timelineService.deleteTimeline(member, gameId, timelineId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT.value())
                .build();
    }
}
