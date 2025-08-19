package com.sports.server.query.support;

import com.sports.server.command.team.domain.PlayerGoalCount;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.query.repository.TimelineQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayerInfoProvider {

    private final TimelineQueryRepository timelineQueryRepository;

    public Map<Long, Integer> getPlayersTotalGoalInfo(List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PlayerGoalCount> results = timelineQueryRepository.countTotalGoalsByPlayerId(playerIds);
        return results.stream()
                .collect(Collectors.toMap(
                        PlayerGoalCount::playerId,
                        dto -> dto.playerTotalGoalCount().intValue()
                ));
    }

    public List<PlayerGoalCountWithRank> getTeamTop20Scorers(Long teamId) {
        Pageable top20Request = PageRequest.of(0, 20);
        return timelineQueryRepository.findTopScorersByTeamId(teamId, top20Request);
    }
}
