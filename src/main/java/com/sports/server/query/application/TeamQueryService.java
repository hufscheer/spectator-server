package com.sports.server.query.application;

import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueStatistics;
import com.sports.server.command.league.domain.LeagueStatisticsRepository;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.*;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.PlayerResponse;
import com.sports.server.query.dto.response.TeamDetailResponse;
import com.sports.server.query.dto.response.TeamResponse;
import com.sports.server.query.repository.TeamQueryDynamicRepository;
import com.sports.server.query.repository.TeamQueryRepository;
import com.sports.server.query.support.PlayerInfoProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamQueryRepository teamQueryRepository;
    private final TeamQueryDynamicRepository teamQueryDynamicRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final EntityUtils entityUtils;
    private final PlayerInfoProvider playerInfoProvider;
    private final GameTeamRepository gameTeamRepository;
    private final LeagueStatisticsRepository leagueStatisticsRepository;

    private static final int ADMISSION_YEAR_START_INDEX = 2;
    private static final int ADMISSION_YEAR_END_INDEX = 4;
    private static final String FIRST_WIN = "우승";
    private static final String SECOND_WIN = "준우승";

    public List<TeamResponse> getAllTeamsByUnits(final List<String> units){
        if (units == null || units.isEmpty()) {
            return teamQueryRepository.findAll()
                    .stream()
                    .map(TeamResponse::new)
                    .toList();
        }

        List<Unit> unitEnums = units.stream()
                .map(Unit::from)
                .toList();

        return teamQueryDynamicRepository.findAllByUnits(unitEnums)
                .stream()
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
                    return PlayerResponse.of(player, totalGoalCount, null);
                })
                .toList();
    }

    public TeamDetailResponse getTeamDetail(Long teamId){
        Team team = entityUtils.getEntity(teamId, Team.class);

        TeamDetailResponse.TeamGameResult teamGameResult = getTeamGameResult(teamId);
        List<TeamDetailResponse.TeamTopScorer> scorers = getTeamTop20Scorers(teamId);
        List<TeamDetailResponse.Trophy> trophies = getTrophies(teamId);

        return new TeamDetailResponse(team, teamGameResult, scorers, trophies);
    }

    private TeamDetailResponse.TeamGameResult getTeamGameResult(Long teamId){
        long winCount = gameTeamRepository.countByTeamIdAndResult(teamId, GameResult.WIN);
        long drawCount = gameTeamRepository.countByTeamIdAndResult(teamId, GameResult.DRAW);
        long loseCount = gameTeamRepository.countByTeamIdAndResult(teamId, GameResult.LOSE);

        return new TeamDetailResponse.TeamGameResult((int) winCount, (int) drawCount, (int) loseCount);
    }

    private List<TeamDetailResponse.TeamTopScorer> getTeamTop20Scorers(Long teamId){
        List<PlayerGoalCountWithRank> topScorers = playerInfoProvider.getTeamTop20Scorers(teamId);

        return topScorers.stream()
                .map(ts -> new TeamDetailResponse.TeamTopScorer(
                        ts.playerId(),
                        ts.studentNumber().substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX),
                        ts.rank().intValue(),
                        ts.playerName(),
                        ts.goalCount().intValue()
                ))
                .toList();
    }

    private List<TeamDetailResponse.Trophy> getTrophies(Long teamId) {
        List<LeagueStatistics> statistics = leagueStatisticsRepository.findTrophiesByTeamId(teamId);

        return statistics.stream()
                .flatMap(stat -> {
                    League league = stat.getLeague();

                    Team firstWinnerTeam = stat.getFirstWinnerTeam();
                    if (firstWinnerTeam != null && firstWinnerTeam.getId().equals(teamId)) {
                        return Stream.of(new TeamDetailResponse.Trophy(league.getId(), league.getName(), FIRST_WIN));
                    }

                    Team secondWinnerTeam = stat.getSecondWinnerTeam();
                    if (secondWinnerTeam != null && secondWinnerTeam.getId().equals(teamId)) {
                        return Stream.of(new TeamDetailResponse.Trophy(league.getId(), league.getName(), SECOND_WIN));
                    }
                    return Stream.empty();
                })
                .toList();
    }
}
