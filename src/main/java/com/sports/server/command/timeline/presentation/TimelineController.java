package com.sports.server.command.timeline.presentation;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.dto.TimelineRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games/{gameId}/timelines")
@RequiredArgsConstructor
public class TimelineController {
    private final TimelineService timelineService;

    @PostMapping("/score")
    @ResponseStatus(HttpStatus.CREATED)
    public void createScoreTimeline(@PathVariable Long gameId,
                                    @RequestBody TimelineRequest.RegisterScore request, Member member) {
        timelineService.register(member, gameId, request);
    }

    @PostMapping("/replacement")
    @ResponseStatus(HttpStatus.CREATED)
    public void createReplacementTimeline(@PathVariable Long gameId,
                                          @RequestBody TimelineRequest.RegisterReplacement request, Member member) {
        timelineService.register(member, gameId, request);
    }

    @PostMapping("/progress")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProgressTimeline(@PathVariable Long gameId,
                                       @RequestBody TimelineRequest.RegisterProgress request, Member member) {
        timelineService.register(member, gameId, request);
    }

    @PostMapping("/pk")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPkTimeline(@PathVariable Long gameId,
                                 @RequestBody TimelineRequest.RegisterPk request, Member member) {
        timelineService.register(member, gameId, request);
    }

    @PostMapping("/warning-card")
    @ResponseStatus(HttpStatus.CREATED)
    public void createWarningCardTimeline(@PathVariable Long gameId,
                                          @RequestBody TimelineRequest.RegisterWarningCard request, Member member) {
        timelineService.register(member, gameId, request);
    }

    @DeleteMapping("/{timelineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimeline(@PathVariable Long gameId,
                               @PathVariable Long timelineId, Member member) {
        timelineService.deleteTimeline(member, gameId, timelineId);
    }
}
