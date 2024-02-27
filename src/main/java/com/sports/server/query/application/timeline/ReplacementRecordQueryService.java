package com.sports.server.query.application.timeline;

import com.sports.server.query.dto.mapper.RecordMapper;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.repository.ReplacementRecordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplacementRecordQueryService implements RecordQueryService {

    private final ReplacementRecordQueryRepository replacementRecordQueryRepository;
    private final RecordMapper recordMapper;

    @Override
    public List<RecordResponse> findByGameId(Long gameId) {
        return replacementRecordQueryRepository.findByGameId(gameId)
                .stream()
                .map(recordMapper::toRecordResponse)
                .toList();
    }
}
