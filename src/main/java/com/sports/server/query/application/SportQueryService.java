package com.sports.server.query.application;

import com.sports.server.query.dto.response.SportResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SportQueryService {

    private final SportQueryRepository sportQueryRepository;

    public List<SportResponse> findAll() {
        return sportQueryRepository.findAll().stream().map(SportResponse::new).toList();
    }
}
