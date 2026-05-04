package com.sports.server.command.cheertalk.application;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.cheertalk.domain.CheerTalk;
import com.sports.server.command.cheertalk.domain.CheerTalkRepository;
import com.sports.server.command.cheertalk.infra.AiSeedMessageGenerator;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.GameTeamRepository;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class AiSeedServiceTest {

    private CheerTalkRepository cheerTalkRepository;
    private GameTeamRepository gameTeamRepository;
    private EntityUtils entityUtils;
    private AiSeedMessageGenerator messageGenerator;
    private ApplicationEventPublisher eventPublisher;
    private AiSeedService aiSeedService;

    private Game soccerGame;
    private Game basketballGame;
    private Game finishedGame;
    private List<GameTeam> gameTeams;

    @BeforeEach
    void setUp() {
        cheerTalkRepository = mock(CheerTalkRepository.class);
        gameTeamRepository = mock(GameTeamRepository.class);
        entityUtils = mock(EntityUtils.class);
        messageGenerator = mock(AiSeedMessageGenerator.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        aiSeedService = new AiSeedService(
                cheerTalkRepository, gameTeamRepository, entityUtils,
                messageGenerator, eventPublisher
        );

        soccerGame = mockGame(GameState.PLAYING, SportType.SOCCER);
        basketballGame = mockGame(GameState.PLAYING, SportType.BASKETBALL);
        finishedGame = mockGame(GameState.FINISHED, SportType.SOCCER);
        gameTeams = mockGameTeams();
    }

    @Nested
    @DisplayName("발화 불가 조건")
    class CannotPublish {

        @Test
        @DisplayName("축구가 아닌 경기는 발화하지 않는다")
        void 축구_아닌_경기_스킵() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(basketballGame);

            aiSeedService.publish(1L, AiSeedTriggerType.GOAL, 1L, "민준");

            verify(cheerTalkRepository, never()).save(any());
        }

        @Test
        @DisplayName("종료된 경기는 발화하지 않는다")
        void 종료된_경기_스킵() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(finishedGame);

            aiSeedService.publish(1L, AiSeedTriggerType.GOAL, 1L, "민준");

            verify(cheerTalkRepository, never()).save(any());
        }

        @Test
        @DisplayName("경기당 5회 초과 시 발화하지 않는다")
        void 최대_횟수_초과_스킵() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(soccerGame);
            when(gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(1L)).thenReturn(gameTeams);
            when(cheerTalkRepository.countAiSeedsByGameTeamIds(anyList())).thenReturn(5L);

            aiSeedService.publish(1L, AiSeedTriggerType.GOAL, 1L, "민준");

            verify(cheerTalkRepository, never()).save(any());
        }

        @Test
        @DisplayName("마지막 AI seed 후 3분 이내면 발화하지 않는다")
        void 간격_부족_스킵() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(soccerGame);
            when(gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(1L)).thenReturn(gameTeams);
            when(cheerTalkRepository.countAiSeedsByGameTeamIds(anyList())).thenReturn(1L);

            CheerTalk recentSeed = mock(CheerTalk.class);
            when(recentSeed.getCreatedAt()).thenReturn(LocalDateTime.now().minusSeconds(30));
            when(cheerTalkRepository.findLastAiSeed(anyList())).thenReturn(Optional.of(recentSeed));

            aiSeedService.publish(1L, AiSeedTriggerType.GOAL, 1L, "민준");

            verify(cheerTalkRepository, never()).save(any());
        }

        @Test
        @DisplayName("SCHEDULED 트리거에서 최근 2분 내 유저 발화가 있으면 발화하지 않는다")
        void 유저_발화_있으면_SCHEDULED_스킵() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(soccerGame);
            when(gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(1L)).thenReturn(gameTeams);
            when(cheerTalkRepository.countAiSeedsByGameTeamIds(anyList())).thenReturn(0L);
            when(cheerTalkRepository.findLastAiSeed(anyList())).thenReturn(Optional.empty());
            when(cheerTalkRepository.existsUserCheerTalkAfter(anyList(), any(LocalDateTime.class)))
                    .thenReturn(true);

            aiSeedService.publish(1L, AiSeedTriggerType.SCHEDULED, null, null);

            verify(cheerTalkRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("정상 발화")
    class Publish {

        @Test
        @DisplayName("GOAL 트리거는 유저 발화 여부와 무관하게 발화한다")
        void GOAL_무조건_발화() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(soccerGame);
            when(gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(1L)).thenReturn(gameTeams);
            when(cheerTalkRepository.countAiSeedsByGameTeamIds(anyList())).thenReturn(0L);
            when(cheerTalkRepository.findLastAiSeed(anyList())).thenReturn(Optional.empty());
            when(cheerTalkRepository.existsUserCheerTalkAfter(anyList(), any(LocalDateTime.class)))
                    .thenReturn(true);
            when(messageGenerator.generate(any(), any(), any())).thenReturn("민준 뭐냐 ㄷㄷ");

            aiSeedService.publish(1L, AiSeedTriggerType.GOAL, 1L, "민준");

            verify(cheerTalkRepository).save(any(CheerTalk.class));
            verify(eventPublisher).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("조건 충족 시 CheerTalk을 저장하고 이벤트를 발행한다")
        void 정상_발화() {
            when(entityUtils.getEntity(1L, Game.class)).thenReturn(soccerGame);
            when(gameTeamRepository.findAllByGameIdForUpdateOrderByAsc(1L)).thenReturn(gameTeams);
            when(cheerTalkRepository.countAiSeedsByGameTeamIds(anyList())).thenReturn(0L);
            when(cheerTalkRepository.findLastAiSeed(anyList())).thenReturn(Optional.empty());
            when(cheerTalkRepository.existsUserCheerTalkAfter(anyList(), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(messageGenerator.generate(any(), any(), any())).thenReturn("경영 가자");

            aiSeedService.publish(1L, AiSeedTriggerType.SCHEDULED, null, null);

            verify(cheerTalkRepository).save(any(CheerTalk.class));
            verify(eventPublisher).publishEvent(any(Object.class));
        }
    }

    private Game mockGame(GameState state, SportType sportType) {
        Game game = mock(Game.class);
        League league = mock(League.class);
        when(game.getState()).thenReturn(state);
        when(game.getLeague()).thenReturn(league);
        when(league.getSportType()).thenReturn(sportType);
        return game;
    }

    private List<GameTeam> mockGameTeams() {
        GameTeam gt1 = mock(GameTeam.class);
        GameTeam gt2 = mock(GameTeam.class);
        Team team1 = mock(Team.class);
        Team team2 = mock(Team.class);
        when(gt1.getId()).thenReturn(1L);
        when(gt2.getId()).thenReturn(2L);
        when(gt1.getTeam()).thenReturn(team1);
        when(gt2.getTeam()).thenReturn(team2);
        when(team1.getName()).thenReturn("경영");
        when(team2.getName()).thenReturn("체육");
        return List.of(gt1, gt2);
    }
}