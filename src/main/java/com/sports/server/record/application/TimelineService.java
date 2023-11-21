package com.sports.server.record.application;

import com.sports.server.record.domain.Record;
import com.sports.server.record.domain.RecordRepository;
import com.sports.server.record.dto.response.TimelineResponse;
import com.sports.server.sport.domain.Quarter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TimelineService {

    private final RecordRepository recordRepository;

    public List<TimelineResponse> getTimeline(final Long gameId) {
        List<Record> records = recordRepository.findByGameId(gameId);
        Map<Quarter, List<Record>> groupedByQuarter = records.stream()
                .collect(groupingBy(Record::getScoredQuarter));
        return groupedByQuarter.keySet()
                .stream()
                .sorted(Comparator.comparingLong(Quarter::getId).reversed())
                .map(quarter -> new TimelineResponse(quarter, groupedByQuarter.get(quarter)))
                .toList();
    }
}
