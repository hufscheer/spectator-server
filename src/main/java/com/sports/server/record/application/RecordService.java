package com.sports.server.record.application;

import com.sports.server.game.domain.Game;
import com.sports.server.record.domain.RecordRepository;
import com.sports.server.record.dto.response.RecordResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private final RecordRepository recordRepository;

    public List<RecordResponseDto> findAllRecordsWithGame(final Game game) {
        return recordRepository.findAllByGame(game).stream().map(RecordResponseDto::new).toList();
    }
}
