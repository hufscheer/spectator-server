package com.sports.server.query.application;

import static java.util.stream.Collectors.toMap;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.*;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.util.StudentNumber;

import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.dto.response.TopScorerResponse;
import com.sports.server.query.repository.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sports.server.query.support.PlayerInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueQueryService {

    private final LeagueQueryRepository leagueQueryRepository;
    private final TeamQueryDynamicRepositoryImpl teamDynamicRepository;
    private final GameQueryRepository gameQueryRepository;
    private final LeagueStatisticsQueryRepository leagueStatisticsQueryRepository;
    private final EntityUtils entityUtils;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LeagueTeamQueryRepository leagueTeamQueryRepository;
    private final PlayerInfoProvider playerInfoProvider;
    private final LeagueTopScorerRepository leagueTopScorerRepository;

    public List<LeagueResponse> findLeagues(LeagueQueryRequestDto queryRequestDto) {
        List<League> leagues = leagueQueryRepository.findLeagues(queryRequestDto);
        if (leagues.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> leagueIds = leagues.stream().map(League::getId).toList();
        Map<Long, String> firstWinnerTeamsInfo = leagueStatisticsQueryRepository.findWinnerTeamInfoByLeagueIds(leagueIds).stream()
                .collect(Collectors.toMap(
                        LeagueWinnerInfo::leagueId,
                        LeagueWinnerInfo::winnerTeamName
                ));

        return leagues.stream().map(league -> {
            String winnerTeamName = firstWinnerTeamsInfo.get(league.getId());
            return new LeagueResponse(league, winnerTeamName);
        }).toList();
    }

    public LeagueRecentSummaryResponse findRecentSummary(Integer year, Integer recordLimit, Integer topScorerLimit) {
        int safeRecordLimit = Math.max(recordLimit, 0);
        int safeTopScorerLimit = Math.max(topScorerLimit, 0);

        LocalDateTime now = LocalDateTime.now();
        int targetYear = getTargetYear(year, now);
        LocalDateTime yearStart = LocalDateTime.of(targetYear, 1, 1, 0, 0);
        LocalDateTime yearEnd = yearStart.plusYears(1);

        List<LeagueRecentSummaryResponse.LeagueRecord> records = safeRecordLimit == 0
                ? Collections.emptyList()
                : leagueQueryRepository.findRecentFinishedLeagues(yearStart, yearEnd, now, PageRequest.of(0, safeRecordLimit)).stream()
                .map(LeagueRecentRecordResult::toResponse)
                .toList();

        List<PlayerGoalCountWithRank> topScorerResults = safeTopScorerLimit == 0
                ? Collections.emptyList()
                : leagueTopScorerRepository.findTopPlayersByYearWithTotalGoals(targetYear, PageRequest.of(0, safeTopScorerLimit));

        Map<Long, String> unitByPlayerId = getUnitByPlayerId(topScorerResults.stream()
                .map(PlayerGoalCountWithRank::playerId)
                .toList());

        List<LeagueRecentSummaryResponse.TopScorer> topScorers = topScorerResults.stream()
                .map(topScorer -> new LeagueRecentSummaryResponse.TopScorer(
                        topScorer.playerId(),
                        StudentNumber.extractAdmissionYear(topScorer.studentNumber()),
                        topScorer.rank().intValue(),
                        topScorer.playerName(),
                        unitByPlayerId.getOrDefault(topScorer.playerId(), ""),
                        topScorer.goalCount().intValue()
                ))
                .toList();

        return new LeagueRecentSummaryResponse(records, topScorers);
    }

    private int getTargetYear(Integer year, LocalDateTime now) {
        if (year != null) {
            return year;
        }
        return leagueQueryRepository.findRecentFinishedLeagueYears(now, PageRequest.of(0, 1)).stream()
                .findFirst()
                .orElse(now.getYear());
    }

    private Map<Long, String> getUnitByPlayerId(List<Long> playerIds) {
        if (playerIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, TeamPlayer> latestTeamPlayerByPlayerId = teamPlayerRepository.findAllByPlayerIds(playerIds).stream()
                .collect(toMap(
                        teamPlayer -> teamPlayer.getPlayer().getId(),
                        Function.identity(),
                        (first, second) -> first.getId() > second.getId() ? first : second
                ));

        return latestTeamPlayerByPlayerId.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getTeam().getUnit().getName()
                ));
    }

    public List<LeagueTeamResponse> findTeamsByLeagueRound(Long leagueId, Integer round) {
        League league = entityUtils.getEntity(leagueId, League.class);

        return teamDynamicRepository.findByLeagueAndRound(league, round).stream()
                .map(leagueTeam -> new LeagueTeamResponse(leagueTeam.getTeam(), leagueTeam.getId()))
                .toList();
    }

    public LeagueDetailResponse findLeagueDetail(Long leagueId) {
        return leagueQueryRepository.findById(leagueId)
                .map(league -> LeagueDetailResponse.of(league, teamDynamicRepository.findByLeagueAndRound(league, null).size()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
    }

    public List<PlayerResponse> findPlayersByLeagueTeam(Long leagueTeamId) {
        LeagueTeam leagueTeam = entityUtils.getEntity(leagueTeamId, LeagueTeam.class);
        Team team = leagueTeam.getTeam();

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(team.getId());
        if (teamPlayers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> playerIds = teamPlayers.stream().map(tp -> tp.getPlayer().getId()).toList();

        Map<Long, Integer> goalCountMap = playerInfoProvider.getPlayersTotalGoalInfo(playerIds);
        return teamPlayers.stream().map(teamPlayer -> {
            int totalGoals = goalCountMap.getOrDefault(teamPlayer.getPlayer().getId(), 0);
            return PlayerResponse.of(teamPlayer, totalGoals);
        }).toList();
    }

    public List<LeagueResponseWithInProgressGames> findLeaguesByManager(final Member member) {
        List<League> leagues = leagueQueryRepository.findByManager(member);
        Map<League, List<Game>> gamesForLeagues = getPlayingGamesOfLeagues(leagues);

        return leagues.stream()
                .map(league -> LeagueResponseWithInProgressGames.of(league, LeagueProgress.fromDate(LocalDateTime.now(), league).getDescription(), gamesForLeagues.get(league)))
                .toList();
    }

    private Map<League, List<Game>> getPlayingGamesOfLeagues(List<League> leagues) {
        if (leagues.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> leagueIds = leagues.stream().map(League::getId).toList();
        List<Game> games = gameQueryRepository.findPlayingGamesByLeagueIdsWithGameTeams(leagueIds);
        Map<Long, List<Game>> gamesByLeagueId = games.stream()
                .collect(Collectors.groupingBy(game -> game.getLeague().getId()));

        return leagues.stream()
                .collect(Collectors.toMap(
                        league -> league,
                        league -> gamesByLeagueId.getOrDefault(league.getId(), Collections.emptyList())
                ));
    }

    public LeagueResponseWithGames findLeagueAndGames(final Long leagueId) {
        League league = leagueQueryRepository.findByIdWithLeagueTeam(leagueId).orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
        List<Game> games = gameQueryRepository.findByLeagueWithGameTeams(league);
        return LeagueResponseWithGames.of(league, games);
    }

    public List<LeagueResponseToManage> findLeaguesByManagerToManage(final Member manager) {
        List<League> leagues = leagueQueryRepository.findByManagerToManage(manager);

        return leagues.stream()
                .sorted(new LeagueProgressComparator(LocalDateTime.now())).map(LeagueResponseToManage::of)
                .toList();
    }

    public LeagueStatisticsResponse findLeagueStatistic(Long leagueId) {
        entityUtils.getEntity(leagueId, League.class);
        LeagueStatistics statistics = leagueStatisticsQueryRepository.findByLeagueId(leagueId).orElseThrow(() -> new NotFoundException("리그 통계 데이터가 아직 업데이트되지 않았습니다."));

        Map<Long, LeagueTeam> leagueTeamsInfo = leagueTeamQueryRepository.findByLeagueId(leagueId).stream().collect(Collectors.toMap(lt -> lt.getTeam().getId(), lt -> lt));

        return LeagueStatisticsResponse.builder().firstWinnerTeam(createLeagueTeamResponseForWinner(statistics.getFirstWinnerTeam(), leagueTeamsInfo)).secondWinnerTeam(createLeagueTeamResponseForWinner(statistics.getSecondWinnerTeam(), leagueTeamsInfo)).mostCheeredTeam(LeagueTeamResponse.ofWithCheerCount(findLeagueTeamFor(statistics.getMostCheeredTeam(), leagueTeamsInfo))).mostCheerTalksTeam(LeagueTeamResponse.ofWithTotalTalkCount(findLeagueTeamFor(statistics.getMostCheerTalksTeam(), leagueTeamsInfo))).build();
    }

    private LeagueTeamResponse createLeagueTeamResponseForWinner(Team team, Map<Long, LeagueTeam> leagueTeamsInfo) {
        if (team == null) {
            return null;
        }
        LeagueTeam leagueTeam = findLeagueTeamFor(team, leagueTeamsInfo);
        return new LeagueTeamResponse(team, leagueTeam.getId());
    }

    private LeagueTeam findLeagueTeamFor(Team team, Map<Long, LeagueTeam> leagueTeamsInfo) {
        if (team == null) {
            return null;
        }
        LeagueTeam leagueTeam = leagueTeamsInfo.get(team.getId());
        if (leagueTeam == null) {
            throw new NotFoundException("리그팀을 찾을 수 없습니다: " + team.getName());
        }
        return leagueTeam;
    }

    public List<TopScorerResponse> findTop20ScorersByLeagueId(Long leagueId) {
        return leagueTopScorerRepository.findByLeagueId(leagueId).stream()
                .map(TopScorerResponse::from).toList();
    }

    public List<TopScorerResponse> findTopScorersByYear(Integer year, Integer limit) {
        List<PlayerGoalCountWithRank> results = leagueTopScorerRepository.findTopPlayersByYearWithTotalGoals(year, PageRequest.of(0, limit));

        return results.stream()
                .map(result -> TopScorerResponse.of(result.playerId(), result.studentNumber(), result.playerName(), result.goalCount().intValue(), result.rank().intValue()))
                .toList();
    }
}
