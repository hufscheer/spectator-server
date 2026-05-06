package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.application.AiSeedService;
import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import com.sports.server.command.timeline.domain.TimelineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-seed.provider", havingValue = "openrouter")
public class AiSeedEventListener {

    private static final int GOAL_DELAY_MIN_SECONDS = 20;
    private static final int GOAL_DELAY_MAX_SECONDS = 90;
    private static final int SECOND_HALF_DELAY_MIN_SECONDS = 30;
    private static final int SECOND_HALF_DELAY_MAX_SECONDS = 90;

    private final AiSeedService aiSeedService;
    private final TimelineRepository timelineRepository;
    private final TaskScheduler taskScheduler;

    @Transactional(readOnly = true)
    @TransactionalEventListener
    public void handle(TimelineCreatedEvent event) {
        switch (event.type()) {
            case SCORE -> handleGoal(event);
            case GAME_PROGRESS -> handleGameProgress(event);
            default -> { }
        }
    }

    private void handleGoal(TimelineCreatedEvent event) {
        Timeline timeline = timelineRepository.findById(event.timelineId()).orElse(null);
        if (!(timeline instanceof ScoreTimeline scoreTimeline)) {
            return;
        }

        String scorerName = scoreTimeline.getScorer().getPlayer().getName();
        Long scoringGameTeamId = scoreTimeline.getScorer().getGameTeam().getId();

        int delay = randomDelay(GOAL_DELAY_MIN_SECONDS, GOAL_DELAY_MAX_SECONDS);
        taskScheduler.schedule(
                () -> aiSeedService.publish(event.gameId(), AiSeedTriggerType.GOAL, scoringGameTeamId, scorerName),
                Instant.now().plusSeconds(delay)
        );
    }

    private void handleGameProgress(TimelineCreatedEvent event) {
        Timeline timeline = timelineRepository.findById(event.timelineId()).orElse(null);
        if (!(timeline instanceof GameProgressTimeline progressTimeline)) {
            return;
        }

        if (progressTimeline.getGameProgressType() != GameProgressType.QUARTER_START) {
            return;
        }
        if (progressTimeline.getRecordedQuarter() != SoccerQuarter.SECOND_HALF) {
            return;
        }

        int delay = randomDelay(SECOND_HALF_DELAY_MIN_SECONDS, SECOND_HALF_DELAY_MAX_SECONDS);
        taskScheduler.schedule(
                () -> aiSeedService.publish(event.gameId(), AiSeedTriggerType.SECOND_HALF_START, null, null),
                Instant.now().plusSeconds(delay)
        );
    }

    private int randomDelay(int minSeconds, int maxSeconds) {
        return ThreadLocalRandom.current().nextInt(minSeconds, maxSeconds + 1);
    }
}