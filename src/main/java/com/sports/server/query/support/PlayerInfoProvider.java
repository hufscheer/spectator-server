package com.sports.server.query.support;

import com.sports.server.command.team.domain.PlayerGoalCount;
import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.query.dto.PlayerAssistCountWithRank;
import com.sports.server.query.dto.PlayerTeamAssistCount;
import com.sports.server.query.dto.PlayerTeamGoalCount;
import com.sports.server.query.dto.TeamTopScorerResult;
import com.sports.server.query.repository.TimelineQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayerInfoProvider {

    private final TimelineQueryRepository timelineQueryRepository;

    public Map<Long, Integer> getPlayersTotalGoalInfo(List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyMap();

        List<PlayerGoalCount> results = timelineQueryRepository.countTotalGoalsByPlayerId(playerIds);
        return results.stream()
                .collect(Collectors.toMap(
                        PlayerGoalCount::playerId,
                        dto -> dto.playerTotalGoalCount().intValue()
                ));
    }

    public Map<Long, Integer> getPlayersGoalInfoInTeam(List<Long> playerIds, Long teamId) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyMap();

        List<PlayerGoalCount> results = timelineQueryRepository.countTotalGoalsByPlayerIdInTeam(playerIds, teamId);
        return results.stream()
                .collect(Collectors.toMap(
                        PlayerGoalCount::playerId,
                        dto -> dto.playerTotalGoalCount().intValue()
                ));
    }

    public List<PlayerGoalCountWithRank> getLeagueTopScorers(Long leagueId, int size) {
        Pageable sizeRequest = PageRequest.of(0, size);
        return timelineQueryRepository.findTopScorersByLeagueId(leagueId, sizeRequest);
    }

    /**
     * 대회에서 선수마다 대표 팀을 하나 고른다. 그 대회에서 가장 많이 득점한 팀이고,
     * 득점이 같으면 마지막으로 득점한 팀이다. 종료된 경기만 센다.
     */
    public Map<Long, Long> getLeagueScorerTeamIds(Long leagueId, List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyMap();

        Comparator<PlayerTeamGoalCount> representative = Comparator
                .comparing(PlayerTeamGoalCount::goalCount)
                .thenComparing(PlayerTeamGoalCount::lastScoreTimelineId);

        return timelineQueryRepository.countLeagueGoalsByPlayerAndTeam(leagueId, playerIds).stream()
                .collect(Collectors.toMap(
                        PlayerTeamGoalCount::playerId,
                        Function.identity(),
                        BinaryOperator.maxBy(representative)
                ))
                .values().stream()
                .collect(Collectors.toMap(PlayerTeamGoalCount::playerId, PlayerTeamGoalCount::teamId));
    }

    public List<PlayerAssistCountWithRank> getLeagueTopAssists(Long leagueId, int size) {
        Pageable sizeRequest = PageRequest.of(0, size);
        return timelineQueryRepository.findTopAssistsByLeagueId(leagueId, sizeRequest);
    }

    /**
     * 대회에서 선수마다 대표 팀을 하나 고른다. 그 대회에서 가장 많이 도움한 팀이고,
     * 도움이 같으면 마지막으로 도움한 팀이다. 종료된 경기만 센다.
     */
    public Map<Long, Long> getLeagueAssistTeamIds(Long leagueId, List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) return Collections.emptyMap();

        Comparator<PlayerTeamAssistCount> representative = Comparator
                .comparing(PlayerTeamAssistCount::assistCount)
                .thenComparing(PlayerTeamAssistCount::lastAssistTimelineId);

        return timelineQueryRepository.countLeagueAssistsByPlayerAndTeam(leagueId, playerIds).stream()
                .collect(Collectors.toMap(
                        PlayerTeamAssistCount::playerId,
                        Function.identity(),
                        BinaryOperator.maxBy(representative)
                ))
                .values().stream()
                .collect(Collectors.toMap(PlayerTeamAssistCount::playerId, PlayerTeamAssistCount::teamId));
    }

    public Map<Long, List<PlayerGoalCountWithRank>> getTeamsTopScorers(List<Long> teamIds, int size) {
        if (teamIds == null || teamIds.isEmpty()) return Collections.emptyMap();
        List<TeamTopScorerResult> teamTopScorers = timelineQueryRepository.findTopScorersByTeamIds(teamIds);

        return teamTopScorers.stream()
                .collect(Collectors.groupingBy(
                        TeamTopScorerResult::teamId,
                        Collectors.mapping(
                                TeamTopScorerResult::playerGoalCountWithRank,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(size).toList()
                                )
                        )
                ));
    }
}
