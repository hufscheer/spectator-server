package com.sports.server.command.game.application;

import java.time.LocalDateTime;
import java.util.List;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.application.LeagueStatisticService;
import com.sports.server.command.league.domain.Round;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GameStatusScheduler {

    private final GameService gameService;
    private final LeagueStatisticService leagueStatisticService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleUpdateGameStatusToFinish() {
        LocalDateTime now = LocalDateTime.now();
        List<Game> finishedGames = gameService.updateGameStatusToFinish(now);

        updateLeagueStatisticsForFinalGames(finishedGames);
    }

    private void updateLeagueStatisticsForFinalGames(List<Game> finishedGames) {
        finishedGames.stream()
                .filter(game -> Round.FINAL.equals(game.getRound()) && game.getLeague() != null)
                .forEach(leagueStatisticService::updateLeagueStatisticFromFinalGame);
    }
}