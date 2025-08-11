package com.sports.server.query.application;

import static java.util.stream.Collectors.toMap;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueProgress;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;

import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.NotFoundException;

import com.sports.server.query.dto.response.*;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.LeagueQueryRepository;
import com.sports.server.query.repository.TeamDynamicRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeagueQueryService {

    private final LeagueQueryRepository leagueQueryRepository;
    private final TeamDynamicRepository teamDynamicRepository;
    private final GameQueryRepository gameQueryRepository;
    private final LeagueStatisticsQueryRepository leagueStatisticsQueryRepository;
    private final EntityUtils entityUtils;
    private final LeagueTeamQueryRepository leagueTeamQueryRepository;

    public List<LeagueResponse> findLeagues(Integer year) {
        return leagueQueryRepository.findByYear(year)
                .stream()
                .map(LeagueResponse::new)
                .toList();
    }

    public List<LeagueTeamResponse> findTeamsByLeagueRound(Long leagueId, Integer round) {
        League league = entityUtils.getEntity(leagueId, League.class);

        return teamDynamicRepository.findByLeagueAndRound(league, round)
                .stream()
                .map(LeagueTeamResponse::new)
                .toList();
    }

    public LeagueDetailResponse findLeagueDetail(Long leagueId) {
        return leagueQueryRepository.findById(leagueId)
                .map(league -> LeagueDetailResponse.of(league,
                        teamDynamicRepository.findByLeagueAndRound(league, null).size()))
                .orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
    }

    public List<LeagueResponseWithInProgressGames> findLeaguesByManager(final Member member) {
        List<League> leagues = leagueQueryRepository.findByManager(member);
        Map<League, List<Game>> gamesForLeagues = getGamesForLeague(leagues);

        return leagues.stream()
                .map(league -> LeagueResponseWithInProgressGames.of(
                        league,
                        LeagueProgress.getProgressDescription(LocalDateTime.now(), league),
                        gamesForLeagues.get(league)))
                .toList();
    }

    private Map<League, List<Game>> getGamesForLeague(List<League> leagues) {
        return leagues.stream()
                .collect(toMap(league -> league,
                        gameQueryRepository::findPlayingGamesByLeagueWithGameTeams));
    }

    public LeagueResponseWithGames findLeagueAndGames(final Long leagueId) {
        League league = leagueQueryRepository.findByIdWithLeagueTeam(leagueId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 리그입니다"));
        List<Game> games = gameQueryRepository.findByLeagueWithGameTeams(league);
        return LeagueResponseWithGames.of(league, games);
    }

    public List<LeagueResponseToManage> findLeaguesByManagerToManage(final Member manager) {
        List<League> leagues = leagueQueryRepository.findByManagerToManage(manager);

        Comparator<League> comparator = Comparator.comparing(
                league -> leagueProgressOrderMap.get(
                        LeagueProgress.getProgressDescription(LocalDateTime.now(), league)));

        return leagues.stream()
                .sorted(comparator)
                .map(LeagueResponseToManage::of)
                .toList();
    }

    public LeagueStatisticsResponse findLeagueStatistic(Long leagueId) {
        LeagueStatistics statistics = leagueStatisticsQueryRepository.findByLeagueId(leagueId);
        List<LeagueTeam> leagueTeams = leagueTeamQueryRepository.findByLeagueId(leagueId);

        return LeagueStatisticsResponse.builder()
                .leagueStatisticsId(statistics.getId())
                .firstWinnerTeam(TeamResponse.from(statistics.getFirstWinnerTeam()))
                .secondWinnerTeam(TeamResponse.from(statistics.getSecondWinnerTeam()))
                .mostCheeredTeam(createTeamResponseWithStats(statistics.getMostCheeredTeam(), leagueTeams))
                .mostCheerTalksTeam(createTeamResponseWithStats(statistics.getMostCheerTalksTeam(), leagueTeams))
                .build();
    }

    private TeamResponse createTeamResponseWithStats(Team team, List<LeagueTeam> leagueTeams) {
        LeagueTeam leagueTeam = leagueTeams.stream()
                .filter(lt -> lt.getTeam().equals(team))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("LeagueTeam not found"));

        return TeamResponse.from(leagueTeam);
    }

    public static Map<String, Integer> leagueProgressOrderMap = Map.ofEntries(
            Map.entry(LeagueProgress.IN_PROGRESS.getDescription(), 1),
            Map.entry(LeagueProgress.BEFORE_START.getDescription(), 2),
            Map.entry(LeagueProgress.FINISHED.getDescription(), 3)
    );
}
