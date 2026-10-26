package com.sports.server.command.game.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.league.application.LeagueStatisticsService;
import com.sports.server.command.league.application.LeagueService;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTopScorerRecalculationEvent;
import com.sports.server.command.league.domain.Round;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameStatusScheduler {

    private final GameService gameService;
    private final LeagueStatisticsService leagueStatisticsService;
    private final ApplicationEventPublisher eventPublisher;
    private final LeagueService leagueService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleUpdateGameStatusToFinish() {
        List<Long> finishedGameIds = gameService.updateGameStatusToFinish(LocalDateTime.now());
        if (!finishedGameIds.isEmpty()) {
            manualUpdateLeagueStatisticsForFinalGames(finishedGameIds);
        }
    }

    private void updateLeagueStatisticsForFinishedGames(List<Game> finishedGames) {
        finishedGames.stream()
                .filter(game -> Round.FINAL.equals(game.getRound()) && game.getLeague() != null)
                .forEach(game -> {
                    leagueStatisticsService.updateLeagueStatisticFromFinalGame(game.getId());
                    leagueService.updateTotalCheerCountsAndTotalTalkCount(game.getLeague().getId());
                });
        // 득점왕은 결승만이 아니라 대회 안 어느 경기든 끝나면 다시 센다
        finishedGames.stream()
                .map(Game::getLeague)
                .filter(Objects::nonNull)
                .map(League::getId)
                .distinct()
                .forEach(this::recalculateTopScorers);
    }

    public void updateLeagueStatisticsIfNeeded(Game game) {
        if (GameState.FINISHED != game.getState()) {
            return;
        }
        updateLeagueStatisticsForFinishedGames(List.of(game));
    }

    public void rollbackLeagueStatisticsIfNeeded(Game game) {
        if (game.getLeague() == null) {
            return;
        }
        if (Round.FINAL == game.getRound()) {
            leagueStatisticsService.rollbackLeagueStatisticForFinalGame(game.getId());
            leagueService.updateTotalCheerCountsAndTotalTalkCount(game.getLeague().getId());
        }
        // 종료가 취소된 경기는 득점왕 집계에서 빠진다. 비우지 않고 남은 종료 경기로 다시 센다
        recalculateTopScorers(game.getLeague().getId());
    }

    public void refreshTopScorersIfFinished(Game game) {
        if (GameState.FINISHED != game.getState() || game.getLeague() == null) {
            return;
        }
        recalculateTopScorers(game.getLeague().getId());
    }

    // 종료가 커밋된 뒤에 센다. 트랜잭션 밖에서 부르면 바로 센다
    private void recalculateTopScorers(Long leagueId) {
        eventPublisher.publishEvent(new LeagueTopScorerRecalculationEvent(leagueId));
    }

    public void manualUpdateLeagueStatisticsForFinalGames(List<Long> gameIds) {
        List<Game> games = gameService.determineResultsAndGet(gameIds);
        updateLeagueStatisticsForFinishedGames(games);
    }
}
