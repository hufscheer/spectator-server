package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkCreateEvent;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.infra.AiSeedMessageGenerator;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-seed.provider", havingValue = "openrouter")
public class AiSeedService {

    private static final int MAX_SEEDS_PER_GAME = 5;
    private static final int SILENCE_MINUTES = 2;
    private static final int MIN_INTERVAL_MINUTES = 3;
    private static final int MESSAGE_ATTEMPTS = 2;

    private final CheerTalkRepository cheerTalkRepository;
    private final GameTeamRepository gameTeamRepository;
    private final GameRepository gameRepository;
    private final AiSeedMessageGenerator messageGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    public void publish(Long gameId, AiSeedTriggerType triggerType,
                        Long scoringGameTeamId, String scorerName) {
        Game game = gameRepository.findByIdWithLeague(gameId)
                .orElseThrow(() -> new NotFoundException("Game을(를) 찾을 수 없습니다"));

        if (!canPublish(game)) {
            return;
        }

        List<GameTeam> gameTeams = gameTeamRepository.findAllByGameIdWithTeamOrderByAsc(gameId);
        List<Long> gameTeamIds = gameTeams.stream().map(GameTeam::getId).toList();

        if (!isReadyForNextSeed(gameTeamIds, triggerType)) {
            return;
        }

        GameTeam selectedTeam = selectTeam(triggerType, scoringGameTeamId, gameTeams);
        String teamName = selectedTeam.getTeam().getName();
        String message = pickUnusedMessage(triggerType, teamName, scorerName, gameTeamIds);
        if (message == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            CheerTalk aiSeed = CheerTalk.createAiSeed(message, selectedTeam.getId());
            cheerTalkRepository.save(aiSeed);
            eventPublisher.publishEvent(new CheerTalkCreateEvent(aiSeed, gameId));
        });

        log.info("AI Seed 발화: gameId={}, trigger={}, team={}, message={}",
                gameId, triggerType, teamName, message);
    }

    /**
     * 같은 경기에서 이미 나온 문장은 다시 내보내지 않는다.
     *
     * <p>모델이 같은 상황에 같은 말을 내놓는다. 한 경기 9건 중 3건이 "후반도 이대로 간다 ㅋ"
     * 였던 적이 있다 — 전체 비율로는 작아도 한 경기만 보는 관객에게는 바로 티가 난다.
     * 한 번 더 뽑아 보고 그래도 겹치면 이번 발화는 거른다. 억지로 내보내느니 조용한 게 낫다.
     */
    private String pickUnusedMessage(AiSeedTriggerType triggerType, String teamName,
                                     String scorerName, List<Long> gameTeamIds) {
        Set<String> published = Set.copyOf(cheerTalkRepository.findAiSeedContents(gameTeamIds));

        for (int attempt = 0; attempt < MESSAGE_ATTEMPTS; attempt++) {
            String candidate = messageGenerator.generate(triggerType, teamName, scorerName);
            if (!published.contains(candidate)) {
                return candidate;
            }
        }

        log.info("AI Seed 중복으로 건너뜀: trigger={}, team={}", triggerType, teamName);
        return null;
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
        return isMandatoryTrigger(triggerType) || !hasRecentUserCheerTalk(gameTeamIds);
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
        if (isMandatoryTrigger(triggerType) && scoringGameTeamId != null) {
            return gameTeams.stream()
                    .filter(gt -> gt.getId().equals(scoringGameTeamId))
                    .findFirst()
                    .orElse(randomTeam(gameTeams));
        }
        return randomTeam(gameTeams);
    }

    private boolean isMandatoryTrigger(AiSeedTriggerType triggerType) {
        return triggerType == AiSeedTriggerType.GOAL || triggerType == AiSeedTriggerType.OWN_GOAL;
    }

    private GameTeam randomTeam(List<GameTeam> gameTeams) {
        int index = ThreadLocalRandom.current().nextInt(gameTeams.size());
        return gameTeams.get(index);
    }
}