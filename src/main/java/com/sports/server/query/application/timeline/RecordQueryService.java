package com.sports.server.query.application.timeline;

import com.sports.server.query.dto.response.RecordResponse;

import java.util.List;

public interface RecordQueryService {

    List<RecordResponse> findByGameId(Long gameId);
}
