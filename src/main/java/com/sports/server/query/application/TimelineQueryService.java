package com.sports.server.query.application;

import com.sports.server.command.sport.domain.Quarter;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.TimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineQueryService {

    private final ReplacementRecordQueryService replacementRecordQueryService;
    private final ScoreRecordQueryService scoreRecordQueryService;

    public List<TimelineResponse> getTimeline(final Long gameId) {
        List<RecordResponse> records = new ArrayList<>();
        records.addAll(replacementRecordQueryService.findByGameId(gameId));
        records.addAll(scoreRecordQueryService.findByGameId(gameId));

        Map<Quarter, List<RecordResponse>> groupedByQuarter = records.stream()
                .sorted(Comparator.comparingInt(RecordResponse::recordedAt).reversed())
                .collect(groupingBy(RecordResponse::quarter));
        return groupedByQuarter.keySet()
                .stream()
                .sorted(Comparator.comparingLong(Quarter::getId).reversed())
                .map(quarter -> new TimelineResponse(quarter.getName(), groupedByQuarter.get(quarter)))
                .toList();

    }
}
