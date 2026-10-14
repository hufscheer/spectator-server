package com.sports.server.command.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.clearInvocations;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/push-notification-fixture.sql")
@DisplayName("타임라인 기록 후 푸시 알림은")
class PushNotificationTimelineTest extends ServiceTest {

    private static final String TEAM_FOLLOWER_TOKEN = "token-team-follower";
    private static final String GAME_FOLLOWER_TOKEN = "token-game-follower";
    private static final String BOTH_FOLLOWER_TOKEN = "token-both-follower";
    private static final String BASKETBALL_FOLLOWER_TOKEN = "token-basketball-follower";
    private static final String ORGANIZATION_FOLLOWER_TOKEN = "token-organization-follower";
    private static final String ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN = "token-organization-and-team-follower";

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

    // setUp 이 테스트마다 같은 토큰으로 기기를 등록해서, 실제 제한기(토큰당 10분 20회)는 테스트가 늘면 걸린다
    @MockBean
    private PushDeviceRegisterRateLimiter pushDeviceRegisterRateLimiter;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        follow(ORGANIZATION_FOLLOWER_TOKEN, PushPlatform.IOS, List.of(), List.of(), List.of(1L));
        follow(ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN, PushPlatform.ANDROID, List.of(1L), List.of(), List.of(1L));
    }

    @Nested
    @DisplayName("경기 시작")
    class GameStart {

        @Test
        void 첫_쿼터가_시작되면_팀이나_경기를_팔로우한_기기에만_한_번씩_보낸다() {
            // when
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_START));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 전반 시작");
            assertThat(message.body()).isEqualTo("팀A vs 팀B · 테스트 리그 4강");
            assertThat(message.data()).containsEntry("url", "/org/1/soccer/games/1")
                    .containsEntry("gameId", "1")
                    .containsEntry("type", "GAME_START");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN,
                            ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN);
        }

        @Test
        void 팀과_학교를_모두_팔로우한_기기에는_팀_팔로워로_한_번만_보낸다() {
            // when
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_START));

            // then
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .filteredOn(ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN::equals)
                    .hasSize(1);
        }

        @Test
        void 농구_경기도_보낸다() {
            // when
            timelineService.register(manager, BASKETBALL_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(), GameProgressType.QUARTER_START));

            // then
            assertThat(sentMessage().title()).isEqualTo("🏀 경기 시작");
            assertThat(sentMessage().data()).containsEntry("url", "/org/1/basketball/games/4");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactly(BASKETBALL_FOLLOWER_TOKEN);
        }

        @Test
        void 연장전_시작은_보내지_않는다() {
            // given
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.QUARTER_END));
            clearInvocations(pushSender);

            // when
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.EXTRA_TIME.name(), GameProgressType.QUARTER_START));

            // then
            verify(pushSender, never()).send(any(), anyList());
        }
    }

    @Nested
    @DisplayName("학교 팔로우")
    class OrganizationFollow {

        @Test
        void 학교만_팔로우한_기기에는_경기_시작과_종료도_보내지_않는다() {
            // when
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_START));
            timelineService.register(manager, SOCCER_LEADING_TWO_TO_ONE, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            ArgumentCaptor<List<PushRecipient>> captor = recipientsCaptor();
            verify(pushSender, times(2)).send(any(), captor.capture());
            assertThat(captor.getAllValues()).flatExtracting(recipients -> recipients)
                    .extracting(PushRecipient::token)
                    .doesNotContain(ORGANIZATION_FOLLOWER_TOKEN);
        }
    }

    @Nested
    @DisplayName("축구 후반 시작")
    class SecondHalfStart {

        @Test
        void 대진과_대회명으로_알리고_학교_팔로워에는_보내지_않는다() {
            // when
            timelineService.register(manager, SOCCER_FIRST_HALF_ENDED, new TimelineRequest.RegisterProgress(
                    45, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.QUARTER_START));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 후반 시작");
            assertThat(message.body()).isEqualTo("팀A vs 팀B · 테스트 리그 4강");
            assertThat(message.data()).containsEntry("url", "/org/1/soccer/games/3")
                    .containsEntry("type", "QUARTER_START");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN,
                            ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN);
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
            // 학교만 팔로우한 기기는 골을 받지 않는다
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN,
                            ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN);
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
    @DisplayName("쿼터 종료")
    class QuarterEnd {

        @Test
        void 현재_점수와_쿼터_이름으로_알린다() {
            // given
            jdbcTemplate.update("UPDATE game_teams SET score = 21 WHERE id = 11");
            jdbcTemplate.update("UPDATE game_teams SET score = 18 WHERE id = 12");

            startBasketballQuarter(BasketballQuarter.FIRST_QUARTER);

            // when
            endBasketballQuarter(BasketballQuarter.FIRST_QUARTER);

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("🏀 1쿼터 종료");
            assertThat(message.body()).isEqualTo("팀C 21 : 18 팀D");
            assertThat(message.data()).containsEntry("url", "/org/1/basketball/games/6")
                    .containsEntry("type", "QUARTER_END");
        }

        @Test
        void 팀이나_경기를_팔로우한_기기에만_보내고_학교_팔로워에는_보내지_않는다() {
            // given
            follow("token-basketball-game-follower", PushPlatform.IOS, List.of(), List.of(BASKETBALL_PLAYING));

            startBasketballQuarter(BasketballQuarter.FIRST_QUARTER);

            // when
            endBasketballQuarter(BasketballQuarter.FIRST_QUARTER);

            // then
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(BASKETBALL_FOLLOWER_TOKEN, "token-basketball-game-follower");
        }

        @ParameterizedTest
        @EnumSource(value = BasketballQuarter.class, names = {"SECOND_QUARTER", "THIRD_QUARTER"})
        void 두_번째와_세_번째_쿼터_종료도_알린다(BasketballQuarter quarter) {
            // given
            advanceBasketballTo(quarter);

            // when
            endBasketballQuarter(quarter);

            // then
            assertThat(sentMessage().title()).isEqualTo("🏀 " + quarter.getDisplayName() + " 종료");
        }

        @Test
        void 사쿼터_종료는_보내지_않는다() {
            // given
            advanceBasketballTo(BasketballQuarter.FOURTH_QUARTER);

            // when
            endBasketballQuarter(BasketballQuarter.FOURTH_QUARTER);

            // then
            verify(pushSender, never()).send(any(), anyList());
        }

        // 연장전은 쿼터 종료를 따로 기록할 수 없고(canHaveQuarterEnd=false) 경기 종료로만 끝난다
        @ParameterizedTest
        @EnumSource(value = BasketballQuarter.class, names = {"FOURTH_QUARTER", "OVERTIME"})
        void 마지막_쿼터에서_경기를_끝내면_경기_종료_알림_하나만_보낸다(BasketballQuarter quarter) {
            // given
            advanceBasketballTo(quarter);

            // when
            timelineService.register(manager, BASKETBALL_PLAYING, new TimelineRequest.RegisterProgress(
                    10, SportType.BASKETBALL, quarter.name(), GameProgressType.GAME_END));

            // then
            assertThat(sentMessage().title()).isEqualTo("🏀 경기 종료");
        }

        @Test
        void 축구의_전반전_종료는_현재_점수와_함께_학교_팔로워를_제외하고_알린다() {
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_START));
            clearInvocations(pushSender);
            jdbcTemplate.update("UPDATE game_teams SET score = 2 WHERE id = 1");
            jdbcTemplate.update("UPDATE game_teams SET score = 1 WHERE id = 2");

            // when
            timelineService.register(manager, SOCCER_PRE_GAME, new TimelineRequest.RegisterProgress(
                    45, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), GameProgressType.QUARTER_END));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 전반 종료");
            assertThat(message.body()).isEqualTo("팀A 2 : 1 팀B");
            assertThat(message.data()).containsEntry("type", "QUARTER_END");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN,
                            ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN);
        }

        @Test
        void 축구의_후반전_종료는_보내지_않는다() {
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.QUARTER_END));

            // then
            verify(pushSender, never()).send(any(), anyList());
        }

        // 앞 쿼터를 차례로 시작·종료한 뒤 목표 쿼터를 시작한다. 앞 쿼터 종료 알림은 비워서 센다
        private void advanceBasketballTo(BasketballQuarter target) {
            for (BasketballQuarter quarter : BasketballQuarter.values()) {
                startBasketballQuarter(quarter);
                if (quarter == target) {
                    break;
                }
                endBasketballQuarter(quarter);
            }
            clearInvocations(pushSender);
        }

        private void startBasketballQuarter(BasketballQuarter quarter) {
            timelineService.register(manager, BASKETBALL_PLAYING, new TimelineRequest.RegisterProgress(
                    0, SportType.BASKETBALL, quarter.name(), GameProgressType.QUARTER_START));
        }

        private void endBasketballQuarter(BasketballQuarter quarter) {
            timelineService.register(manager, BASKETBALL_PLAYING, new TimelineRequest.RegisterProgress(
                    10, SportType.BASKETBALL, quarter.name(), GameProgressType.QUARTER_END));
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
            assertThat(message.title()).isEqualTo("⚽ 경기 종료 · 팀A 승리");
            assertThat(message.body()).isEqualTo("팀A 2 : 1 팀B");
            assertThat(message.data()).containsEntry("type", "GAME_END");
            assertThat(sentRecipients()).extracting(PushRecipient::token)
                    .containsExactlyInAnyOrder(TEAM_FOLLOWER_TOKEN, GAME_FOLLOWER_TOKEN, BOTH_FOLLOWER_TOKEN,
                            ORGANIZATION_AND_TEAM_FOLLOWER_TOKEN);
        }

        @Test
        void 무승부면_승리한_팀을_붙이지_않는다() {
            // when
            timelineService.register(manager, SOCCER_SECOND_HALF, new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END));

            // then
            PushMessage message = sentMessage();
            assertThat(message.title()).isEqualTo("⚽ 경기 종료");
            assertThat(message.body()).isEqualTo("팀A 1 : 1 팀B");
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
        follow(token, platform, teamIds, gameIds, List.of());
    }

    private void follow(String token, PushPlatform platform, List<Long> teamIds, List<Long> gameIds,
                        List<Long> organizationIds) {
        String deviceKey = pushDeviceService.register(token, new PushDeviceRegisterRequest(token, platform)).deviceKey();
        pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(teamIds, gameIds, organizationIds));
    }

    private PushMessage sentMessage() {
        ArgumentCaptor<PushMessage> captor = ArgumentCaptor.forClass(PushMessage.class);
        verify(pushSender).send(captor.capture(), anyList());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<PushRecipient>> recipientsCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    @SuppressWarnings("unchecked")
    private List<PushRecipient> sentRecipients() {
        ArgumentCaptor<List<PushRecipient>> captor = ArgumentCaptor.forClass(List.class);
        verify(pushSender).send(any(), captor.capture());
        return captor.getValue();
    }
}
