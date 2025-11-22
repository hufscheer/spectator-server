package com.sports.server.command.game.application;

import java.time.LocalDateTime;
import java.util.List;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.application.LeagueStatisticsService;
import com.sports.server.command.league.application.LeagueTopScorerService;
import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.domain.Round;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameStatusScheduler {

    private final GameService gameService;
    private final LeagueStatisticsService leagueStatisticsService;
    private final LeagueTopScorerService leagueTopScorerService;
    private final LeagueService leagueService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleUpdateGameStatusToFinish() {
        List<Game> finishedGames = gameService.updateGameStatusToFinish(LocalDateTime.now());
        finishedGames.forEach(Game::determineResult);
        updateLeagueStatisticsForFinalGames(finishedGames);
    }

    private void updateLeagueStatisticsForFinalGames(List<Game> finishedGames) {
        finishedGames.stream()
                .filter(game -> Round.FINAL.equals(game.getRound()) && game.getLeague() != null)
                .forEach(game -> {
                    leagueStatisticsService.updateLeagueStatisticFromFinalGame(game.getId());
                    leagueTopScorerService.updateTopScorersForLeague(game.getLeague().getId());
                    leagueService.updateTotalCheerCountsAndTotalTalkCount(game.getLeague().getId());
                });
    }

    public void manualUpdateLeagueStatisticsForFinalGames(List<Long> gameIds) {
        List<Game> games = gameService.findGamesByIds(gameIds);
        updateLeagueStatisticsForFinalGames(games);
    }
}