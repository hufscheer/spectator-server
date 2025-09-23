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

import com.sports.server.command.team.domain.PlayerGoalCountWithRank;
import com.sports.server.query.dto.request.LeagueQueryRequestDto;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.dto.response.TopScorerResponse;
import com.sports.server.query.repository.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        Map<Long, String> firstWinnerTeamsInfo = leagueStatisticsQueryRepository.findWinnerTeamInfoByLeagueIds(leagueIds).stream().collect(Collectors.toMap(LeagueWinnerInfo::leagueId, LeagueWinnerInfo::winnerTeamName));

        return leagues.stream().map(league -> {
            String winnerTeamName = firstWinnerTeamsInfo.get(league.getId());
            return new LeagueResponse(league, winnerTeamName);
        }).toList();
    }

    public List<LeagueTeamResponse> findTeamsByLeagueRound(Long leagueId, Integer round) {
        League league = entityUtils.getEntity(leagueId, League.class);

        return teamDynamicRepository.findByLeagueAndRound(league, round).stream().map(leagueTeam -> new LeagueTeamResponse(leagueTeam.getTeam(), leagueTeam.getId())).toList();
    }

    public LeagueDetailResponse findLeagueDetail(Long leagueId) {
        return leagueQueryRepository.findById(leagueId).map(league -> LeagueDetailResponse.of(league, teamDynamicRepository.findByLeagueAndRound(league, null).size())).orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
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

        return leagues.stream().map(league -> LeagueResponseWithInProgressGames.of(league, LeagueProgress.fromDate(LocalDateTime.now(), league).getDescription(), gamesForLeagues.get(league))).toList();
    }

    private Map<League, List<Game>> getPlayingGamesOfLeagues(List<League> leagues) {
        List<Long> leagueIds = leagues.stream().map(League::getId).toList();
        List<Game> games = gameQueryRepository.findPlayingGamesByLeagueIdsWithGameTeams(leagueIds);

        return leagues.stream().collect(toMap(league -> league, league -> getPlayingGamesOfLeague(league, games)));
    }

    private List<Game> getPlayingGamesOfLeague(League league, List<Game> games) {
        return games.stream().filter(game -> game.getLeague().equals(league)).toList();
    }

    public LeagueResponseWithGames findLeagueAndGames(final Long leagueId) {
        League league = leagueQueryRepository.findByIdWithLeagueTeam(leagueId).orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
        List<Game> games = gameQueryRepository.findByLeagueWithGameTeams(league);
        return LeagueResponseWithGames.of(league, games);
    }

    public List<LeagueResponseToManage> findLeaguesByManagerToManage(final Member manager) {
        List<League> leagues = leagueQueryRepository.findByManagerToManage(manager);

        return leagues.stream().sorted(new LeagueProgressComparator(LocalDateTime.now())).map(LeagueResponseToManage::of).toList();
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
        return leagueTopScorerRepository.findByLeagueId(leagueId).stream().map(TopScorerResponse::from).toList();
    }

    public List<TopScorerResponse> findTopScorersByYear(Integer year, Integer limit) {
        List<PlayerGoalCountWithRank> results = leagueTopScorerRepository.findTopPlayersByYearWithTotalGoals(year, PageRequest.of(0, limit));

        return results.stream().map(result -> TopScorerResponse.of(result.playerId(), result.studentNumber(), result.playerName(), result.goalCount().intValue(), result.rank().intValue())).toList();
    }
}
