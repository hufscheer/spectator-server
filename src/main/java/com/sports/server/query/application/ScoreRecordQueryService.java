package com.sports.server.query.application;

import com.sports.server.query.dto.mapper.RecordMapper;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.repository.GameTeamQueryRepository;
import com.sports.server.query.repository.ScoreRecordQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScoreRecordQueryService implements RecordQueryService {

    private final ScoreRecordQueryRepository scoreRecordQueryRepository;
    private final GameTeamQueryRepository gameTeamQueryRepository;
    private final RecordMapper recordMapper;

    @Override
    public List<RecordResponse> findByGameId(Long gameId) {
        ScoreHistory scoreHistory = ScoreHistory.of(
                scoreRecordQueryRepository.findByGameId(gameId),
                gameTeamQueryRepository.findAllByGameWithTeam(gameId)
        );
        return scoreHistory.getScoreRecordsOrderByTimeDesc()
                .stream()
                .map(record -> recordMapper.toRecordResponse(record, scoreHistory.getSnapshot(record)))
                .toList();
    }
}
