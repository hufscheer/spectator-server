package com.sports.server.query.application;

import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.TimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineQueryService {

    private final List<RecordQueryService> recordQueryServices;

    public List<TimelineResponse> getTimeline(final Long gameId) {
        Map<Quarter, List<RecordResponse>> records = getRecordsGroupByQuarter(gameId);
        return records.keySet()
                .stream()
                .sorted(comparingLong(Quarter::getId).reversed())
                .map(quarter -> new TimelineResponse(
                        quarter.getName(),
                        records.get(quarter)
                )).toList();

    }

    private Map<Quarter, List<RecordResponse>> getRecordsGroupByQuarter(Long gameId) {
        return recordQueryServices.stream()
                .flatMap(recordQueryService -> recordQueryService.findByGameId(gameId).stream())
                .sorted(comparingInt(RecordResponse::recordedAt).reversed())
                .collect(groupingBy(RecordResponse::quarter));
    }
}
