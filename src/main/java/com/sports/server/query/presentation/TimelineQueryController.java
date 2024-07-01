package com.sports.server.query.presentation;

import com.sports.server.query.application.timeline.TimelineQueryService;
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

    /**
     * TODO 마이그레이션이 완료되면 삭제될 API
     */
    @Deprecated
    @GetMapping("/games/{gameId}/timeline")
    public ResponseEntity<List<TimelineResponse>> getTimeline(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getTimeline(gameId));
    }

    /**
     * Timeline 엔티티를 사용하는 타임라인 조회 API
     * TODO 마이그레이션이 완료되면 v2 워딩을 삭제
     */
    @GetMapping("/games/{gameId}/timeline/v2")
    public ResponseEntity<List<TimelineResponse>> getTimelines(@PathVariable final Long gameId) {
        return ResponseEntity.ok(timelineQueryService.getTimelines(gameId));
    }
}
