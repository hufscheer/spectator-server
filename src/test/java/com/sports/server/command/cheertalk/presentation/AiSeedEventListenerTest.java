package com.sports.server.command.cheertalk.presentation;

import com.sports.server.command.cheertalk.application.AiSeedService;
import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.TimelineCreatedEvent;
import com.sports.server.command.timeline.domain.TimelineRepository;
import com.sports.server.command.timeline.domain.TimelineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiSeedEventListenerTest {

    private AiSeedService aiSeedService;
    private TimelineRepository timelineRepository;
    private TaskScheduler taskScheduler;
    private AiSeedEventListener listener;

    @BeforeEach
    void setUp() {
        aiSeedService = mock(AiSeedService.class);
        timelineRepository = mock(TimelineRepository.class);
        taskScheduler = mock(TaskScheduler.class);
        listener = new AiSeedEventListener(aiSeedService, timelineRepository, taskScheduler);
    }

    @Nested
    @DisplayName("SCORE 타임라인 이벤트")
    class ScoreEvent {

        @Test
        @DisplayName("SCORE 이벤트면 GOAL 트리거로 예약한다")
        void SCORE_이벤트_GOAL_예약() {
            ScoreTimeline scoreTimeline = mockScoreTimeline("민준", 1L);
            when(timelineRepository.findById(10L)).thenReturn(Optional.of(scoreTimeline));

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.SCORE);
            listener.handle(event);

            verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        @DisplayName("timeline이 존재하지 않으면 무시한다")
        void timeline_없으면_무시() {
            when(timelineRepository.findById(10L)).thenReturn(Optional.empty());

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.SCORE);
            listener.handle(event);

            verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("OWN_GOAL 타임라인 이벤트")
    class OwnGoalEvent {

        @Test
        @DisplayName("OWN_GOAL 이벤트면 OWN_GOAL 트리거로 상대팀에 예약한다")
        void OWN_GOAL_이벤트_상대팀_예약() {
            OwnGoalTimeline ownGoalTimeline = mockOwnGoalTimeline(2L);
            when(timelineRepository.findById(10L)).thenReturn(Optional.of(ownGoalTimeline));

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.OWN_GOAL);
            listener.handle(event);

            ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
            verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

            runnableCaptor.getValue().run();
            verify(aiSeedService).publish(100L, AiSeedTriggerType.OWN_GOAL, 2L, null);
        }

        @Test
        @DisplayName("timeline이 존재하지 않으면 무시한다")
        void timeline_없으면_무시() {
            when(timelineRepository.findById(10L)).thenReturn(Optional.empty());

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.OWN_GOAL);
            listener.handle(event);

            verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("GAME_PROGRESS 타임라인 이벤트")
    class GameProgressEvent {

        @Test
        @DisplayName("SECOND_HALF QUARTER_START면 후반전 트리거로 예약한다")
        void 후반전_시작_예약() {
            GameProgressTimeline progressTimeline = mockProgressTimeline(
                    GameProgressType.QUARTER_START, SoccerQuarter.SECOND_HALF);
            when(timelineRepository.findById(10L)).thenReturn(Optional.of(progressTimeline));

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.GAME_PROGRESS);
            listener.handle(event);

            verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        @DisplayName("FIRST_HALF QUARTER_START면 무시한다")
        void 전반전_시작_무시() {
            GameProgressTimeline progressTimeline = mockProgressTimeline(
                    GameProgressType.QUARTER_START, SoccerQuarter.FIRST_HALF);
            when(timelineRepository.findById(10L)).thenReturn(Optional.of(progressTimeline));

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.GAME_PROGRESS);
            listener.handle(event);

            verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        }

        @Test
        @DisplayName("QUARTER_END면 무시한다")
        void QUARTER_END_무시() {
            GameProgressTimeline progressTimeline = mockProgressTimeline(
                    GameProgressType.QUARTER_END, SoccerQuarter.SECOND_HALF);
            when(timelineRepository.findById(10L)).thenReturn(Optional.of(progressTimeline));

            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.GAME_PROGRESS);
            listener.handle(event);

            verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    @Nested
    @DisplayName("기타 타임라인 이벤트")
    class OtherEvents {

        @Test
        @DisplayName("WARNING_CARD 등 다른 타입은 무시한다")
        void 다른_타입_무시() {
            TimelineCreatedEvent event = new TimelineCreatedEvent(10L, 100L, TimelineType.WARNING_CARD);
            listener.handle(event);

            verify(timelineRepository, never()).findById(any());
            verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        }
    }

    private ScoreTimeline mockScoreTimeline(String scorerName, Long gameTeamId) {
        ScoreTimeline timeline = mock(ScoreTimeline.class);
        LineupPlayer scorer = mock(LineupPlayer.class);
        Player player = mock(Player.class);
        GameTeam gameTeam = mock(GameTeam.class);
        when(timeline.getScorer()).thenReturn(scorer);
        when(scorer.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn(scorerName);
        when(scorer.getGameTeam()).thenReturn(gameTeam);
        when(gameTeam.getId()).thenReturn(gameTeamId);
        return timeline;
    }

    private OwnGoalTimeline mockOwnGoalTimeline(Long opponentGameTeamId) {
        OwnGoalTimeline timeline = mock(OwnGoalTimeline.class);
        GameTeam opponentGameTeam = mock(GameTeam.class);
        when(opponentGameTeam.getId()).thenReturn(opponentGameTeamId);
        when(timeline.getOpponentGameTeam()).thenReturn(opponentGameTeam);
        return timeline;
    }

    private GameProgressTimeline mockProgressTimeline(GameProgressType progressType, SoccerQuarter quarter) {
        GameProgressTimeline timeline = mock(GameProgressTimeline.class);
        when(timeline.getGameProgressType()).thenReturn(progressType);
        when(timeline.getRecordedQuarter()).thenReturn(quarter);
        return timeline;
    }
}