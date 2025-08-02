package com.sports.server.command.league.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueStatistic;
import com.sports.server.command.league.domain.LeagueStatisticRepository;
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
public class LeagueStatisticService {

    private final LeagueStatisticRepository leagueStatisticRepository;
    private final EntityUtils entityUtils;

    @Transactional
    public Long updateLeagueStatisticFromFinalGame(Game finalGame) {
        if (finalGame == null || finalGame.getLeague() == null) {
            throw new IllegalArgumentException("유효한 게임 또는 리그 정보가 없습니다.");
        }
        
        League league = finalGame.getLeague();
        LeagueStatistic leagueStatistic = getOrCreateLeagueStatistic(league);

        updateWinnerTeamsFromGame(finalGame, leagueStatistic);

        updateMostCheeredAndTalkedTeams(league, leagueStatistic);
        
        return leagueStatistic.getId();
    }

    @Transactional(readOnly = true)
    public LeagueStatistic getLeagueStatistic(Long leagueId) {
        League league = entityUtils.getEntity(leagueId, League.class);
        return getOrCreateLeagueStatistic(league);
    }

    private LeagueStatistic getOrCreateLeagueStatistic(League league) {
        LeagueStatistic leagueStatistic = league.getLeagueStatistic();
        if (leagueStatistic == null) {
            leagueStatistic = new LeagueStatistic(league);
            leagueStatisticRepository.save(leagueStatistic);
        }
        return leagueStatistic;
    }
    
    private void updateWinnerTeamsFromGame(Game finalGame, LeagueStatistic leagueStatistic) {
        List<GameTeam> teams = finalGame.getTeams();
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
    
    private void updateMostCheeredAndTalkedTeams(League league, LeagueStatistic leagueStatistic) {
        List<LeagueTeam> leagueTeams = league.getLeagueTeams();
        if (leagueTeams.isEmpty()) {
            return;
        }
        
        // 최다 응원팀 찾기
        leagueTeams.stream()
                .filter(lt -> lt.getTotalCheerCount() != null && lt.getTotalCheerCount() > 0)
                .max(Comparator.comparing(LeagueTeam::getTotalCheerCount))
                .map(LeagueTeam::getTeam)
                .ifPresent(leagueStatistic::updateMostCheeredTeam);
        
        // 최다 응원 톡 팀 찾기
        leagueTeams.stream()
                .filter(lt -> lt.getTotalTalkCount() != null && lt.getTotalTalkCount() > 0)
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