package com.sports.server.query.dto.response;

import java.util.List;

public record TeamSummaryResponse(
        TeamDetailResponse teamDetail,
        List<GameDetailResponse> recentGames
) {
}
