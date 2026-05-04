package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.infra.AiSeedMessageGenerator;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.application.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-seed.provider", havingValue = "openrouter")
public class AiSeedService {

    private static final int MAX_SEEDS_PER_GAME = 5;
    private static final int SILENCE_MINUTES = 2;
    private static final int MIN_INTERVAL_MINUTES = 3;

    private final CheerTalkRepository cheerTalkRepository;
    private final GameTeamRepository gameTeamRepository;
    private final EntityUtils entityUtils;
    private final AiSeedMessageGenerator messageGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void publish(Long gameId, AiSeedTriggerType triggerType,
                        Long scoringGameTeamId, String scorerName) {
        Game game = entityUtils.getEntity(gameId, Game.class);

        if (!canPublish(game)) {
            return;
        }

        List<GameTeam> gameTeams = gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(gameId);
        List<Long> gameTeamIds = gameTeams.stream().map(GameTeam::getId).toList();

        if (!isReadyForNextSeed(gameTeamIds, triggerType)) {
            return;
        }

        GameTeam selectedTeam = selectTeam(triggerType, scoringGameTeamId, gameTeams);
        String teamName = selectedTeam.getTeam().getName();
        String message = messageGenerator.generate(triggerType, teamName, scorerName);

        CheerTalk aiSeed = CheerTalk.createAiSeed(message, selectedTeam.getId());
        cheerTalkRepository.save(aiSeed);
        eventPublisher.publishEvent(new CheerTalkCreateEvent(aiSeed, gameId));

        log.info("AI Seed 발화: gameId={}, trigger={}, team={}, message={}",
                gameId, triggerType, teamName, message);
    }

    private boolean canPublish(Game game) {
        return isSoccerGame(game) && game.getState() != GameState.FINISHED;
    }

    private boolean isReadyForNextSeed(List<Long> gameTeamIds, AiSeedTriggerType triggerType) {
        if (hasReachedMaxSeeds(gameTeamIds)) {
            return false;
        }
        if (isTooSoonSinceLastSeed(gameTeamIds)) {
            return false;
        }
        return triggerType == AiSeedTriggerType.GOAL || !hasRecentUserCheerTalk(gameTeamIds);
    }

    private boolean hasReachedMaxSeeds(List<Long> gameTeamIds) {
        return cheerTalkRepository.countAiSeedsByGameTeamIds(gameTeamIds) >= MAX_SEEDS_PER_GAME;
    }

    private boolean isTooSoonSinceLastSeed(List<Long> gameTeamIds) {
        Optional<CheerTalk> lastSeed = cheerTalkRepository.findLastAiSeed(gameTeamIds);
        return lastSeed.isPresent()
                && lastSeed.get().getCreatedAt().plusMinutes(MIN_INTERVAL_MINUTES).isAfter(LocalDateTime.now());
    }

    private boolean hasRecentUserCheerTalk(List<Long> gameTeamIds) {
        return cheerTalkRepository.existsUserCheerTalkAfter(
                gameTeamIds, LocalDateTime.now().minusMinutes(SILENCE_MINUTES));
    }

    private boolean isSoccerGame(Game game) {
        return game.getLeague().getSportType() == SportType.SOCCER;
    }

    private GameTeam selectTeam(AiSeedTriggerType triggerType, Long scoringGameTeamId, List<GameTeam> gameTeams) {
        if (triggerType == AiSeedTriggerType.GOAL && scoringGameTeamId != null) {
            return gameTeams.stream()
                    .filter(gt -> gt.getId().equals(scoringGameTeamId))
                    .findFirst()
                    .orElse(randomTeam(gameTeams));
        }
        return randomTeam(gameTeams);
    }

    private GameTeam randomTeam(List<GameTeam> gameTeams) {
        int index = ThreadLocalRandom.current().nextInt(gameTeams.size());
        return gameTeams.get(index);
    }
}