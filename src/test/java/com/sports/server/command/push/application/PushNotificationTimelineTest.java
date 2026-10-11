package com.sports.server.command.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.push.domain.PushDevice;
import com.sports.server.command.push.domain.PushDeviceRepository;
import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushPlatform;
import com.sports.server.command.push.domain.PushRecipient;
import com.sports.server.command.push.dto.PushDeviceRegisterRequest;
import com.sports.server.command.push.dto.PushFollowsRequest;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/push-notification-fixture.sql")
@DisplayName("타임라인 기록 후 푸시 알림은")
class PushNotificationTimelineTest extends ServiceTest {

    private static final String TEAM_FOLLOWER_TOKEN = "token-team-follower";
    private static final String GAME_FOLLOWER_TOKEN = "token-game-follower";
    private static final String BOTH_FOLLOWER_TOKEN = "token-both-follower";
    private static final String BASKETBALL_FOLLOWER_TOKEN = "token-basketball-follower";

    private static final long SOCCER_PRE_GAME = 1L;
    private static final long SOCCER_SECOND_HALF = 2L;
    private static final long SOCCER_FIRST_HALF_ENDED = 3L;
    private static final long BASKETBALL_PRE_GAME = 4L;
    private static final long SOCCER_LEADING_TWO_TO_ONE = 5L;
    private static final long BASKETBALL_PLAYING = 6L;

    private static final long TEAM_A_SECOND_HALF_GAME_TEAM = 3L;
    private static final long TEAM_A_PLAYER_LINEUP = 1L;
    private static final long BASKETBALL_GAME_TEAM = 11L;
    private static final long BASKETBALL_PLAYER_LINEUP = 3L;

    @MockBean
    private PushSender pushSender;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    @Autowired
    private PushDeviceService pushDeviceService;

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
        given(pushSender.send(any(), anyList())).willReturn(List.of());

        follow(TEAM_FOLLOWER_TOKEN, PushPlatform.IOS, List.of(1L), List.of());
        follow(GAME_FOLLOWER_TOKEN, PushPlatform.ANDROID, List.of(),
                List.of(SOCCER_PRE_GAME, SOCCER_SECOND_HALF, SOCCER_FIRST_HALF_ENDED, SOCCER_LEADING_TWO_TO_ONE));
        follow(BOTH_FOLLOWER_TOKEN, PushPlatform.WEB, List.of(1L, 2L), List.of(SOCCER_PRE_GAME));
        follow(BASKETBALL_FOLLOWER_TOKEN, PushPlatform.IOS, List.of(3L), List.of());
    }

    @Nested
    @DisplayName("경기 시작")
    class GameStart {

        @Test
        void 첫_쿼터가_시작되면_팀이나_경기를_팔로우한_기기에_한_번씩_보낸다() {
            // when
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_START));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("경기 시작");
            assertThat(message.body()).isEqualTo("팀A vs 팀B · 테스트 리그 4강");
            assertThat(message.data()).containsEntry("url", "/org/1/soccer/games/1")
                    .containsEntry("gameId", "1")
                    .containsEntry("type", "GAME_START");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN);
        }

        @Test
        void 농구_경기도_보낸다() {
            // when
            timelineService.register(manager, BASKETBALL_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(), GameProgressType.QUARTER_START));

            // then
            assertThat(sentMessage().data()).containsEntry("url", "/org/1/basketball/games/4");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactly(BASKETBALL_FOLLOWER_TOKEN);
        }

        @Test
        void 첫_쿼터가_아닌_쿼터_시작은_보내지_않는다() {
            // when
            timelineService.register(manager, SOCCER_FIRST_HALF_ENDED, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.QUARTER_START));

            // then
            verify(pushSender, never()).send(any(), anyList());
        }
    }

    @Nested
    @DisplayName("축구 득점")
    class Goal {

        @Test
        void 득점한_팀과_스냅샷_점수로_알린다() {
            // when
            registerSoccerGoal(false);

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 팀A 골!");
            assertThat(message.body()).isEqualTo("팀A 2 : 1 팀B (후반전 10')");
            assertThat(message.data()).containsEntry("type", "GOAL");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN);
        }

        @Test
        void 자책골은_득점한_상대_팀의_골로_알린다() {
            // when
            registerSoccerGoal(true);

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 팀B 골!");
            assertThat(message.body()).isEqualTo("팀A 1 : 2 팀B (후반전 10')");
        }

        @Test
        void 농구_득점은_보내지_않는다() {
            // when
            timelineService.register(manager, BASKETBALL_PLAYING, new TimelineRequest.RegisterBasketballScore(
                    BASKETBALL_GAME_TEAM, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    BASKETBALL_PLAYER_LINEUP, 3, null, 2));

            // then
            verify(pushSender, never()).send(any(), anyList());
        }

        @Test
        void 기록을_삭제하면_보내지_않는다() {
            // given
            registerSoccerGoal(false);
            Timeline goal = timelineFixtureRepository.findAllLatest(SOCCER_SECOND_HALF).get(0);

            // when
            timelineService.deleteTimeline(manager, SOCCER_SECOND_HALF, goal.getId());

            // then
            verify(pushSender, times(1)).send(any(), anyList());
        }

        private void registerSoccerGoal(boolean ownGoal) {
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterSoccerScore(
                    TEAM_A_SECOND_HALF_GAME_TEAM, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    TEAM_A_PLAYER_LINEUP, 10, null, ownGoal));
        }
    }

    @Nested
    @DisplayName("경기 종료")
    class GameEnd {

        @Test
        void 승리한_팀과_최종_점수로_알린다() {
            // when
            timelineService.register(manager, SOCCER_LEADING_TWO_TO_ONE, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("경기 종료");
            assertThat(message.body()).isEqualTo("팀A 2 : 1 팀B · 팀A 승리");
            assertThat(message.data()).containsEntry("type", "GAME_END");
        }

        @Test
        void 무승부면_승리한_팀을_붙이지_않는다() {
            // when
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            assertThat(sentMessage().body()).isEqualTo("팀A 1 : 1 팀B");
        }
    }

    @Nested
    @DisplayName("전송 결과")
    class Delivery {

        @Test
        void 팔로우한_기기가_없으면_보내지_않는다() {
            // given
            List<Long> followerIds = pushDeviceRepository.findFollowers(List.of(1L, 2L), SOCCER_LEADING_TWO_TO_ONE)
                    .stream().map(PushDevice::getId).toList();
            pushDeviceService.removeAll(followerIds);

            // when
            timelineService.register(manager, SOCCER_LEADING_TWO_TO_ONE, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            verify(pushSender, never()).send(any(), anyList());
        }

        @Test
        void 무효로_돌려받은_토큰의_기기는_지운다() {
            // given
            Long staleId = pushDeviceRepository.findByTokenHash(
                    PushDevice.hashOf(GAME_FOLLOWER_TOKEN)).orElseThrow().getId();
            given(pushSender.send(any(), anyList())).willReturn(List.of(staleId));

            // when
            timelineService.register(manager, SOCCER_LEADING_TWO_TO_ONE, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            assertThat(pushDeviceRepository.findByTokenHash(
                    PushDevice.hashOf(GAME_FOLLOWER_TOKEN))).isEmpty();
        }
    }

    private void follow(String token, PushPlatform platform, List<Long> teamIds, List<Long> gameIds) {
        String deviceKey = pushDeviceService.register(token, new PushDeviceRegisterRequest(token, platform)).deviceKey();
        pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(teamIds, gameIds));
    }

    private PushMessage sentMessage() {
        ArgumentCaptor<PushMessage> captor = ArgumentCaptor.forClass(PushMessage.class);
        verify(pushSender).send(captor.capture(), anyList());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<PushRecipient> sentRecipients() {
        ArgumentCaptor<List<PushRecipient>> captor = ArgumentCaptor.forClass(List.class);
        verify(pushSender).send(any(), captor.capture());
        return captor.getValue();
    }
}
