package com.sports.server.query.application;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.groupingBy;

import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.query.dto.response.TimelineResponse;
import com.sports.server.query.repository.TimelineQueryRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineQueryService {

    private final TimelineQueryRepository timelineQueryRepository;

    public List<TimelineResponse> getTimelines(final Long gameId) {
        Map<Quarter, List<Timeline>> timelines = timelineQueryRepository.findByGameId(gameId)
                .stream()
                .collect(groupingBy(Timeline::getRecordedQuarter));

        return timelines.keySet()
                .stream()
                .sorted(comparingLong(Quarter::getOrder).thenComparing(Quarter::getId).reversed())
                .map(quarter -> TimelineResponse.of(
                        quarter.getName(),
                        timelines.get(quarter)
                )).toList();
    }
}
