package com.sports.server.command.game.application;

import java.time.LocalDateTime;
import java.util.List;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
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
        List<Long> finalGameIds = gameService.updateGameStatusToFinish(LocalDateTime.now());
        if (!finalGameIds.isEmpty()) {
            manualUpdateLeagueStatisticsForFinalGames(finalGameIds);
        }
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

    public void updateLeagueStatisticsIfNeeded(Long gameId, GameState state, Round round) {
        if (GameState.FINISHED != state || Round.FINAL != round) {
            return;
        }
        manualUpdateLeagueStatisticsForFinalGames(List.of(gameId));
    }

    public void updateLeagueStatisticsIfNeeded(Game game) {
        if (GameState.FINISHED != game.getState() || Round.FINAL != game.getRound()) {
            return;
        }
        updateLeagueStatisticsForFinalGames(List.of(game));
    }

    public void rollbackLeagueStatisticsIfNeeded(Game game) {
        if (Round.FINAL != game.getRound() || game.getLeague() == null) {
            return;
        }
        leagueStatisticsService.rollbackLeagueStatisticForFinalGame(game.getId());
        leagueTopScorerService.clearTopScorersForLeague(game.getLeague().getId());
        leagueService.updateTotalCheerCountsAndTotalTalkCount(game.getLeague().getId());
    }

    public void manualUpdateLeagueStatisticsForFinalGames(List<Long> gameIds) {
        List<Game> games = gameService.determineResultsAndGet(gameIds);
        updateLeagueStatisticsForFinalGames(games);
    }
}