package com.sports.server.command.league.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.*;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static com.sports.server.command.game.domain.Game.MINIMUM_TEAMS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueStatisticsService {
    private final LeagueStatisticsRepository leagueStatisticsRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public void updateLeagueStatisticFromFinalGame(Long finalGameId) {
        Game finalGame = entityUtils.getEntity(finalGameId, Game.class);

        League league = finalGame.getLeague();
        LeagueStatistics leagueStatistics = getLeagueStatistics(league);

        updateWinnerTeamsFromGame(finalGame, leagueStatistics);
        updateMostCheeredAndTalkedTeams(league, leagueStatistics);
    }

    private LeagueStatistics getLeagueStatistics(League league) {
        LeagueStatistics leagueStatistics = leagueStatisticsRepository.findByLeagueId(league.getId());
        if (leagueStatistics == null) {
            leagueStatistics = LeagueStatistics.of(league);
            leagueStatisticsRepository.save(leagueStatistics);
        }
        return leagueStatistics;
    }

    private void updateWinnerTeamsFromGame(Game finalGame, LeagueStatistics leagueStatistic) {
        List<GameTeam> teams = gameTeamQueryRepository.findAllByGame(finalGame);
        if (teams.size() < MINIMUM_TEAMS) {
            return;
        }

        League league = leagueStatistic.getLeague();

        updateRankedTeam(teams, GameResult.WIN, 1, league, leagueStatistic::updateFirstWinnerTeam);
        updateRankedTeam(teams, GameResult.LOSE, 2, league, leagueStatistic::updateSecondWinnerTeam);
    }

    private void updateRankedTeam(List<GameTeam> teams,
                                  GameResult targetResult,
                                  int ranking,
                                  League league,
                                  Consumer<Team> statisticsUpdater) {
        teams.stream()
                .filter(gameTeam -> targetResult.equals(gameTeam.getResult()))
                .findFirst()
                .ifPresent(gameTeam -> {
                    Team team = gameTeam.getTeam();
                    statisticsUpdater.accept(team);
                    updateLeagueTeamRanking(league, team, ranking);
                });
    }

    private void updateMostCheeredAndTalkedTeams(League league, LeagueStatistics leagueStatistic) {
        List<LeagueTeam> leagueTeams = leagueTeamRepository.findByLeagueId(league.getId());
        if (leagueTeams.isEmpty()) {
            return;
        }

        findTeamWithMaxValue(leagueTeams, LeagueTeam::getTotalCheerCount, leagueStatistic::updateMostCheeredTeam);
        findTeamWithMaxValue(leagueTeams, LeagueTeam::getTotalTalkCount, leagueStatistic::updateMostCheerTalksTeam);
    }

    private void findTeamWithMaxValue(List<LeagueTeam> leagueTeams,
                                      ToIntFunction<LeagueTeam> valueExtractor,
                                      Consumer<Team> teamUpdater) {
        leagueTeams.stream()
                .max(Comparator.comparingInt(valueExtractor))
                .map(LeagueTeam::getTeam)
                .ifPresent(teamUpdater);
    }

    private void updateLeagueTeamRanking(League league, Team team, int ranking) {
        leagueTeamRepository.findByLeagueAndTeam(league, team)
                .ifPresent(leagueTeam -> leagueTeam.updateRanking(ranking));
    }
}
