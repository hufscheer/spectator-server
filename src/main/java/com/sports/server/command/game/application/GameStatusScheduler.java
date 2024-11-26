package com.sports.server.command.game.application;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameStatusScheduler {

    private final GameService gameService;

    public GameStatusScheduler(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void scheduleUpdateGameStatusToFinish() {
        gameService.updateGameStatusToFinish(LocalDateTime.now());
    }
}