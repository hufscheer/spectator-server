package com.sports.server.command.league.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueStatistics;
import com.sports.server.command.league.domain.LeagueStatisticsRepository;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueStatisticsService {
    private final LeagueStatisticsRepository leagueStatisticsRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public Long updateLeagueStatisticFromFinalGame(Game finalGame) {
        if (finalGame == null || finalGame.getLeague() == null) {
            throw new IllegalArgumentException("유효한 게임 또는 리그 정보가 없습니다.");
        }

        League league = finalGame.getLeague();
        LeagueStatistics leagueStatistics = getOrCreateLeagueStatistics(league);

        updateWinnerTeamsFromGame(finalGame, leagueStatistics);

        updateMostCheeredAndTalkedTeams(league, leagueStatistics);

        return leagueStatistics.getId();
    }

    private LeagueStatistics getOrCreateLeagueStatistics(League league) {
        LeagueStatistics leagueStatistics = league.getLeagueStatistics();
        if (leagueStatistics == null) {
            leagueStatistics = new LeagueStatistics(league);
            leagueStatisticsRepository.save(leagueStatistics);
        }
        return leagueStatistics;
    }

    private void updateWinnerTeamsFromGame(Game finalGame, LeagueStatistics leagueStatistic) {
        List<GameTeam> teams = finalGame.getGameTeams();
        if (teams.size() < 2) {
            return;
        }

        League league = leagueStatistic.getLeague();

        // 우승팀 설정
        teams.stream()
                .filter(gameTeam -> GameResult.WIN.equals(gameTeam.getResult()))
                .findFirst()
                .ifPresent(winner -> {
                    Team winnerTeam = winner.getTeam();
                    leagueStatistic.updateFirstWinnerTeam(winnerTeam);
                    updateLeagueTeamRanking(league, winnerTeam, 1);
                });

        // 준우승팀 설정
        teams.stream()
                .filter(gameTeam -> GameResult.LOSE.equals(gameTeam.getResult()))
                .findFirst()
                .ifPresent(loser -> {
                    Team loserTeam = loser.getTeam();
                    leagueStatistic.updateSecondWinnerTeam(loserTeam);
                    updateLeagueTeamRanking(league, loserTeam, 2);
                });
    }

    private void updateMostCheeredAndTalkedTeams(League league, LeagueStatistics leagueStatistic) {
        List<LeagueTeam> leagueTeams = league.getLeagueTeams();
        if (leagueTeams.isEmpty()) {
            return;
        }

        // 최다 응원팀 찾기
        leagueTeams.stream()
                .max(Comparator.comparing(LeagueTeam::getTotalCheerCount))
                .map(LeagueTeam::getTeam)
                .ifPresent(leagueStatistic::updateMostCheeredTeam);

        // 최다 응원 톡 팀 찾기
        leagueTeams.stream()
                .max(Comparator.comparing(LeagueTeam::getTotalTalkCount))
                .map(LeagueTeam::getTeam)
                .ifPresent(leagueStatistic::updateMostCheerTalksTeam);
    }

    private void updateLeagueTeamRanking(League league, Team team, int ranking) {
        league.getLeagueTeams().stream()
                .filter(lt -> lt.getTeam().equals(team))
                .findFirst()
                .ifPresent(lt -> lt.updateRanking(ranking));
    }
}
