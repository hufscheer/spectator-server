package com.sports.server.query.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueStatistics;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.*;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.*;
import com.sports.server.query.repository.GameQueryRepository;
import com.sports.server.query.repository.LeagueStatisticsQueryRepository;
import com.sports.server.query.repository.TeamQueryDynamicRepository;
import com.sports.server.query.repository.TeamQueryRepository;
import com.sports.server.query.support.PlayerInfoProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final EntityUtils entityUtils;
    private final PlayerInfoProvider playerInfoProvider;

    private final TeamQueryRepository teamQueryRepository;
    private final TeamQueryDynamicRepository teamQueryDynamicRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final GameTeamRepository gameTeamRepository;
    private final LeagueStatisticsQueryRepository leagueStatisticsQueryRepository;
    private final GameQueryRepository gameQueryRepository;

    private static final String FIRST_WIN = "우승";
    private static final String SECOND_WIN = "준우승";

    private static final int TEAM_DETAIL_TOP_SCORERS_COUNT = 20;
    private static final int TEAM_SUMMARY_TOP_SCORERS_COUNT = 3;

    public List<TeamResponse> getAllTeamsByUnits(final List<String> units){
        List<Team> teams = getTeamsFilteredByUnit(units);
        return teams.stream()
                .map(TeamResponse::new)
                .toList();
    }

    public List<PlayerResponse> getAllTeamPlayers(Long teamId){
        List<TeamPlayer> teamPlayers = teamQueryRepository.findAllTeamPlayer(teamId);
        List<Long> playerIds = teamPlayerRepository.findPlayerIdsByTeamId(teamId);

        Map<Long, Integer> playerTotalGoalCountInfo = playerInfoProvider.getPlayersTotalGoalInfo(playerIds);
        return teamPlayers.stream()
                .map(tp -> {
                    Player player = tp.getPlayer();
                    int totalGoalCount = playerTotalGoalCountInfo.getOrDefault(player.getId(), 0);
                    return PlayerResponse.of(player, tp.getId(), totalGoalCount, null);
                })
                .toList();
    }

    public TeamDetailResponse getTeamDetail(Long teamId){
        Team team = entityUtils.getEntity(teamId, Team.class);

        TeamDetailResponse.TeamGameResult teamGameResult = getTeamGameResults(List.of(teamId)).get(teamId);
        List<TeamDetailResponse.TeamTopScorer> scorers = getTeamTopScorers(List.of(teamId), TEAM_DETAIL_TOP_SCORERS_COUNT).get(teamId);
        List<TeamDetailResponse.Trophy> trophies = getTrophies(List.of(teamId)).get(teamId);
        List<PlayerResponse> teamPlayers = getAllTeamPlayers(teamId);

        return new TeamDetailResponse(team, teamPlayers, teamGameResult, scorers, trophies);
    }

    public List<TeamSummaryResponse> getAllTeamsSummary(final List<String> units){
        List<Team> teams = getTeamsFilteredByUnit(units);
        if (teams.isEmpty()) return Collections.emptyList();

        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        TeamStatistics statistics = getTeamStatistics(teamIds);
        return teams.stream()
                .map(team -> {
                    TeamDetailResponse teamDetail = new TeamDetailResponse(team, statistics, null);
                    List<GameDetailResponse> recentGames = statistics.recentGamesMap()
                            .getOrDefault(team.getId(), Collections.emptyList());
                    return new TeamSummaryResponse(teamDetail, recentGames);
                })
                .toList();
    }

    private TeamStatistics getTeamStatistics(List<Long> teamIds) {
            return new TeamStatistics(
                    getTeamGameResults(teamIds),
                    getTeamTopScorers(teamIds, TEAM_SUMMARY_TOP_SCORERS_COUNT),
                    getTrophies(teamIds),
                    getRecentGames(teamIds)
            );
    }

    private List<Team> getTeamsFilteredByUnit(final List<String> units){
        if (units == null || units.isEmpty()) return teamQueryRepository.findAll();
        List<Unit> unitEnums = Unit.fromNames(units);
        return teamQueryDynamicRepository.findAllByUnits(unitEnums);
    }

    private Map<Long, TeamDetailResponse.TeamGameResult> getTeamGameResults(List<Long> teamIds){
        List<TeamGameResult> results = gameTeamRepository.findGameResultsByTeamIds(teamIds);

        Map<Long, Map<GameResult, Integer>> teamResult = new HashMap<>();
        for (TeamGameResult result : results) {
            teamResult.computeIfAbsent(result.teamId(), teamId -> new HashMap<>())
                    .put(result.result(), result.count() != null ? result.count().intValue() : 0);
        }

        return teamIds.stream()
                .collect(Collectors.toMap(
                        teamId -> teamId,
                        teamId -> {
                            Map<GameResult, Integer> counts = teamResult.getOrDefault(teamId, Collections.emptyMap());
                            return new TeamDetailResponse.TeamGameResult(
                                    counts.getOrDefault(GameResult.WIN, 0),
                                    counts.getOrDefault(GameResult.DRAW, 0),
                                    counts.getOrDefault(GameResult.LOSE, 0)
                            );
                        }
                ));
    }

    private Map<Long, List<TeamDetailResponse.TeamTopScorer>> getTeamTopScorers(List<Long> teamIds, int size){
        Map<Long, List<PlayerGoalCountWithRank>> topScorersMap = playerInfoProvider.getTeamsTopScorers(teamIds, size);

        return teamIds.stream()
                .collect(Collectors.toMap(
                        teamId -> teamId,
                        teamId -> topScorersMap.getOrDefault(teamId, Collections.emptyList())
                                .stream()
                                .map(TeamDetailResponse.TeamTopScorer::new)
                                .toList()
                ));
    }

    private Map<Long, List<TeamDetailResponse.Trophy>> getTrophies(List<Long> teamIds) {
        List<LeagueStatistics> statistics = leagueStatisticsQueryRepository.findTrophiesByTeamIds(teamIds);
        Map<Long, List<TeamDetailResponse.Trophy>> trophiesByTeamId = new HashMap<>();
        Set<Long> targetTeamIds = new HashSet<>(teamIds);

        statistics.forEach(stat -> {
            League league = stat.getLeague();
            Team firstWinner = stat.getFirstWinnerTeam();
            Team secondWinner = stat.getSecondWinnerTeam();

            if (firstWinner != null && targetTeamIds.contains(firstWinner.getId())) {
                trophiesByTeamId.computeIfAbsent(firstWinner.getId(), teamId -> new ArrayList<>())
                        .add(new TeamDetailResponse.Trophy(league.getId(), league.getName(), FIRST_WIN));
            }
            if (secondWinner != null && targetTeamIds.contains(secondWinner.getId())) {
                trophiesByTeamId.computeIfAbsent(secondWinner.getId(), teamId -> new ArrayList<>())
                        .add(new TeamDetailResponse.Trophy(league.getId(), league.getName(), SECOND_WIN));
            }
        });
        return trophiesByTeamId;
    }

    private Map<Long, List<GameDetailResponse>> getRecentGames(List<Long> teamIds) {
        List<Game> recentGames = gameQueryRepository.findRecentGamesByTeamIds(teamIds);
        if (recentGames.isEmpty()) return Collections.emptyMap();

        List<Long> gameIds = recentGames.stream().map(Game::getId).toList();
        List<GameTeam> allGameTeams = gameTeamRepository.findAllByGameIds(gameIds);

        Map<Long, GameDetailResponse> gameDetailsMap = toGameDetailMap(recentGames, allGameTeams);
        return groupGamesByTeam(teamIds, allGameTeams, gameDetailsMap);
    }

    private Map<Long, GameDetailResponse> toGameDetailMap(List<Game> recentGames, List<GameTeam> allGameTeams) {
        Map<Long, List<GameTeam>> gameIdToTeams = allGameTeams.stream()
                .collect(Collectors.groupingBy(gt -> gt.getGame().getId()));

        return recentGames.stream()
                .distinct()
                .collect(Collectors.toMap(
                        Game::getId,
                        game -> {
                            List<GameTeam> teamsInGame = gameIdToTeams.getOrDefault(game.getId(),
                                    Collections.emptyList());
                            return new GameDetailResponse(game, teamsInGame);
                        }
                ));
    }

    private Map<Long, List<GameDetailResponse>> groupGamesByTeam(List<Long> teamIds, List<GameTeam> allGameTeams,
                                                                 Map<Long, GameDetailResponse> gameDetailsMap) {
        Set<Long> targetTeamIds = new HashSet<>(teamIds);
        Map<Long, List<GameTeam>> teamsByTeamId = allGameTeams.stream()
                .filter(gt -> targetTeamIds.contains(gt.getTeam().getId()))
                .collect(Collectors.groupingBy(gt -> gt.getTeam().getId()));

        return teamsByTeamId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(gt -> gameDetailsMap.get(gt.getGame().getId()))
                                .toList()
                ));
    }
}
