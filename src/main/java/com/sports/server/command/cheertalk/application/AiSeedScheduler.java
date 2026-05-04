package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSeedScheduler {

    private static final int SCHEDULED_DELAY_MIN_SECONDS = 0;
    private static final int SCHEDULED_DELAY_MAX_SECONDS = 60;

    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;
    private final CheerTalkRepository cheerTalkRepository;
    private final AiSeedService aiSeedService;
    private final TaskScheduler taskScheduler;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void checkScheduledTrigger() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusMinutes(1);
        LocalDateTime to = now.plusMinutes(6);

        List<Game> upcomingGames = gameRepository.findScheduledSoccerGamesBetween(from, to);

        for (Game game : upcomingGames) {
            if (hasAlreadyPublishedScheduledSeed(game)) {
                continue;
            }

            int delay = ThreadLocalRandom.current().nextInt(SCHEDULED_DELAY_MIN_SECONDS, SCHEDULED_DELAY_MAX_SECONDS + 1);
            taskScheduler.schedule(
                    () -> aiSeedService.publish(game.getId(), AiSeedTriggerType.SCHEDULED, null, null),
                    Instant.now().plusSeconds(delay)
            );

            log.info("SCHEDULED AI Seed 예약: gameId={}, delay={}s", game.getId(), delay);
        }
    }

    private boolean hasAlreadyPublishedScheduledSeed(Game game) {
        List<Long> gameTeamIds = gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(game.getId())
                .stream().map(GameTeam::getId).toList();
        return cheerTalkRepository.countAiSeedsByGameTeamIds(gameTeamIds) > 0;
    }
}