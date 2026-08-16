package com.sports.server.command.timeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.timeline.domain.*;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.command.timeline.exception.TimelineErrorMessage;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@Sql(scripts = "/timeline-fixture.sql")
class TimelineServiceTest extends ServiceTest {
    @Autowired
    private TimelineService timelineService;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityUtils entityUtils;

    private final Long gameId = 1L;
    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 경기의_매니저가_아닌_회원이_타임라인을_등록하려고_하면_예외가_발생한다() {
        // given
        Member nonManager = entityUtils.getEntity(2L, Member.class);
        Long team1Id = 1L;
        Long team1PlayerId = 1L;

        TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                team1PlayerId, 3, null);

        // when & then
        assertThatThrownBy(() -> timelineService.register(nonManager, gameId, request)).isInstanceOf(
                UnauthorizedException.class);

    }

    @DisplayName("득점 타임라인을")
    @Nested
    class CreateTest {
        @Test
        void 팀1이_생성한다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    team1PlayerId, 3, null);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(team1PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(16),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(10),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(SoccerQuarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3));

        }

        @Test
        void 팀2가_생성한다() {
            // given
            Long team2Id = 2L;
            Long team2PlayerId = 6L;

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team2Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    team2PlayerId, 5, null);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(team2PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(15),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(11),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(SoccerQuarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(5));
        }

        @Test
        void 같은_팀_선수를_어시스트로_등록한다() {
            // given
            Long team1Id = 1L;
            Long scorerId = 1L;
            Long assistId = 2L; // 같은 팀1 소속 선수

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    scorerId, 3, assistId);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(scorerId),
                    () -> assertThat(actual.getAssistLineupPlayer().getId()).isEqualTo(assistId));
        }

        @Test
        void 다른_팀_선수를_어시스트로_등록하면_예외가_발생한다() {
            // given
            Long team1Id = 1L;
            Long scorerId = 1L;   // 팀1 선수
            Long assistId = 6L;   // 팀2 선수

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    scorerId, 3, assistId);

            // when & then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void 득점_선수_본인을_어시스트로_등록하면_예외가_발생한다() {
            // given
            Long team1Id = 1L;
            Long scorerId = 1L;

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    scorerId, 3, scorerId);

            // when & then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @DisplayName("자책골 타임라인을")
    @Nested
    class CreateOwnGoalTest {
        @Test
        void team1_선수의_자책골이면_team2가_득점한다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;

            TimelineRequest.RegisterOwnGoal request = new TimelineRequest.RegisterOwnGoal(
                    3, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team1Id, team1PlayerId);

            // when
            timelineService.register(manager, gameId, request);

            // then
            OwnGoalTimeline actual = (OwnGoalTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);

            assertAll(() -> assertThat(actual.getScorer().getId()).isEqualTo(team1PlayerId),
                    () -> assertThat(actual.getSnapshotScore1()).isEqualTo(15),
                    () -> assertThat(actual.getSnapshotScore2()).isEqualTo(11));
        }

        @Test
        void 승부차기에서는_등록할_수_없다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;

            TimelineRequest.RegisterOwnGoal request = new TimelineRequest.RegisterOwnGoal(
                    3, SportType.SOCCER, SoccerQuarter.PENALTY_SHOOTOUT.name(), team1Id, team1PlayerId);

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void 요청한_팀과_다른_팀_선수는_자책골_선수로_등록할_수_없다() {
            // given
            Long team1Id = 1L;
            Long team2PlayerId = 6L; // 팀2 소속 선수

            TimelineRequest.RegisterOwnGoal request = new TimelineRequest.RegisterOwnGoal(
                    3, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team1Id, team2PlayerId);

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void 농구_경기에서는_등록할_수_없다() {
            // given: 농구 경기(game 5)에 자책골 등록 시도
            Long basketballGameId = 5L;
            Long basketballTeamId = 7L;
            Long basketballPlayerId = 17L;

            TimelineRequest.RegisterOwnGoal request = new TimelineRequest.RegisterOwnGoal(
                    3, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(), basketballTeamId, basketballPlayerId);

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, basketballGameId, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @DisplayName("교체 타임라인을")
    @Nested
    class CreateReplacementTest {
        Long team1Id = 1L;
        Long team1OriginPlayerId = 1L;
        Long team1ReplacedPlayerId = 2L;

        Long team2Id = 2L;
        Long team2OriginPlayerId = 6L;
        Long team2ReplacedPlayerId = 7L;

        @Test
        void 팀1에서_생성한다() {
            // given

            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team1OriginPlayerId, team1ReplacedPlayerId, 3, null);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(() -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team1OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team1ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(SoccerQuarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true));
        }

        @Test
        void 팀2에서_생성한다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team2Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team2OriginPlayerId, team2ReplacedPlayerId, 3, null);

            // when
            timelineService.register(manager, gameId, request);

            // then
            ReplacementTimeline actual = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(gameId)
                    .get(0);

            assertAll(() -> assertThat(actual.getOriginLineupPlayer().getId()).isEqualTo(team2OriginPlayerId),
                    () -> assertThat(actual.getReplacedLineupPlayer().getId()).isEqualTo(team2ReplacedPlayerId),
                    () -> assertThat(actual.getRecordedQuarter()).isEqualTo(SoccerQuarter.SECOND_HALF),
                    () -> assertThat(actual.getRecordedAt()).isEqualTo(3),
                    () -> assertThat(actual.getOriginLineupPlayer().isPlaying()).isEqualTo(false),
                    () -> assertThat(actual.getReplacedLineupPlayer().isPlaying()).isEqualTo(true));
        }

        @Test
        void 다른_팀끼리_생성할_수_없다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team2Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team1OriginPlayerId, team2ReplacedPlayerId, 3, null);

            // when then
            assertThatThrownBy(() -> timelineService.register(manager, gameId, request)).isInstanceOf(
                    CustomException.class);
        }
    }

    @Nested
    @DisplayName("게임 진행 타임라인을")
    class GameProgressTimelineTest {
        @Test
        void 생성한다() {
            // given
            Long freshGameId = 4L; // PRE_GAME, SCHEDULED 상태
            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(0, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                    GameProgressType.QUARTER_START);

            // when
            timelineService.register(manager, freshGameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(freshGameId).get(0);
            assertThat(actual).isInstanceOf(GameProgressTimeline.class);
        }

        @Test
        void 쿼터가_진행_중일_때_경기종료를_등록하면_쿼터종료가_자동으로_삽입된다() {
            // given - game 6: SECOND_HALF QUARTER_START 상태 (쿼터 진행 중)
            Long testGameId = 6L;
            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(
                    90, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), GameProgressType.GAME_END);

            int beforeCount = timelineFixtureRepository.findAllLatest(testGameId).size();

            // when
            timelineService.register(manager, testGameId, request);

            // then: QUARTER_END + GAME_END 2개가 추가되어야 함
            List<Timeline> timelines = timelineFixtureRepository.findAllLatest(testGameId);
            assertThat(timelines).hasSize(beforeCount + 2);

            GameProgressTimeline gameEnd = (GameProgressTimeline) timelines.get(0);
            GameProgressTimeline quarterEnd = (GameProgressTimeline) timelines.get(1);

            assertAll(
                    () -> assertThat(gameEnd.getGameProgressType()).isEqualTo(GameProgressType.GAME_END),
                    () -> assertThat(quarterEnd.getGameProgressType()).isEqualTo(GameProgressType.QUARTER_END),
                    () -> assertThat(quarterEnd.getRecordedQuarter()).isEqualTo(SoccerQuarter.SECOND_HALF)
            );
        }
    }

    @DisplayName("승부차기 타임라인을")
    @Nested
    class PkTest {

        @Test
        void 생성한다() {
            // given
            Long teamId = 1L;
            Long teamPlayerId = 1L;
            int recordedAt = 10;

            TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(recordedAt, SportType.SOCCER, SoccerQuarter.PENALTY_SHOOTOUT.name(),
                    teamId, teamPlayerId, true);

            // when
            timelineService.register(manager, gameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertThat(actual).isInstanceOf(PKTimeline.class);

        }
    }

    @DisplayName("농구 득점 타임라인을")
    @Nested
    class BasketballScoreTest {
        private final Long basketballGameId = 5L;
        private final Long teamAId = 7L;
        private final Long playerAId = 17L;

        @Test
        void 농구_3점_득점으로_생성한다() {
            // given
            TimelineRequest.RegisterBasketballScore request = new TimelineRequest.RegisterBasketballScore(
                    teamAId, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    playerAId, 10, null, 3
            );

            // when
            timelineService.register(manager, basketballGameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(basketballGameId).get(0);
            assertThat(actual.getScore()).isEqualTo(3);
        }

        @Test
        void 축구_득점은_항상_1점으로_저장된다() {
            // given
            Long team1Id = 1L;
            Long team1PlayerId = 1L;
            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(
                    team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), team1PlayerId, 3, null
            );

            // when
            timelineService.register(manager, gameId, request);

            // then
            ScoreTimeline actual = (ScoreTimeline) timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertThat(actual.getScore()).isEqualTo(1);
        }
    }

    @DisplayName("경고 타임라인을")
    @Nested
    class WarningCardTest {
        @Test
        void 생성한다() {
            //given
            Long teamId = 1L;
            Long playerId = 1L;
            int recordedAt = 10;

            TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(recordedAt, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), teamId, playerId, WarningCardType.YELLOW);

            //when
            timelineService.register(manager, gameId, request);

            //then
            Timeline actual = timelineFixtureRepository.findAllLatest(gameId).get(0);
            assertAll(() -> Assertions.assertThat(actual).isInstanceOf(WarningCardTimeline.class),
                    () -> Assertions.assertThat(((WarningCardTimeline) actual).getWarningCardType())
                            .isEqualTo(WarningCardType.YELLOW));
        }
    }

    @DisplayName("농구 교체 타임라인을")
    @Nested
    class BasketballReplacementTest {
        private final Long basketballGameId = 5L;
        private final Long basketballTeamId = 7L;
        private final Long originPlayerId = 17L;
        private final Long replacementPlayerId = 21L; // CANDIDATE 선수

        @Test
        void 파울_아웃으로_생성한다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    basketballTeamId, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    originPlayerId, replacementPlayerId, 10, true);

            // when
            timelineService.register(manager, basketballGameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(basketballGameId).get(0);
            assertAll(
                    () -> assertThat(actual).isInstanceOf(BasketballReplacementTimeline.class),
                    () -> assertThat(((BasketballReplacementTimeline) actual).isFoulOut()).isTrue()
            );
        }

        @Test
        void 일반_교체로_생성한다() {
            // given
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    basketballTeamId, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    originPlayerId, replacementPlayerId, 10, false);

            // when
            timelineService.register(manager, basketballGameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(basketballGameId).get(0);
            assertThat(((BasketballReplacementTimeline) actual).isFoulOut()).isFalse();
        }

        @Test
        void 농구_경기가_아니면_등록할_수_없다() {
            // given: 축구 경기(game 1)에 BASKETBALL sportType으로 교체 등록 시도
            Long soccerGameId = 1L;
            Long soccerTeamId = 1L;
            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(
                    soccerTeamId, SportType.BASKETBALL, SoccerQuarter.SECOND_HALF.name(),
                    1L, 2L, 10, false);

            // when & then
            assertThatThrownBy(() -> timelineService.register(manager, soccerGameId, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @DisplayName("파울 타임라인을")
    @Nested
    class FoulTest {
        private final Long basketballGameId = 5L;
        private final Long basketballTeamId = 7L;
        private final Long basketballPlayerId = 17L;

        @Test
        void 생성한다() {
            // given
            TimelineRequest.RegisterFoul request = new TimelineRequest.RegisterFoul(
                    10, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    basketballTeamId, basketballPlayerId);

            // when
            timelineService.register(manager, basketballGameId, request);

            // then
            Timeline actual = timelineFixtureRepository.findAllLatest(basketballGameId).get(0);
            assertThat(actual).isInstanceOf(FoulTimeline.class);
        }

        @Test
        void 참여하지_않는_선수는_파울을_받을_수_없다() {
            // given
            Long otherGamePlayerId = 1L; // 축구 game 1 소속 선수 (농구 game 5에 없음)
            TimelineRequest.RegisterFoul request = new TimelineRequest.RegisterFoul(
                    10, SportType.BASKETBALL, BasketballQuarter.FIRST_QUARTER.name(),
                    basketballTeamId, otherGamePlayerId);

            // when & then
            assertThatThrownBy(() -> timelineService.register(manager, basketballGameId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 농구_경기가_아니면_파울을_등록할_수_없다() {
            // given: 축구 경기(game 1)에 파울 등록 시도
            Long soccerGameId = 1L;
            Long soccerTeamId = 1L;
            Long soccerPlayerId = 1L;
            TimelineRequest.RegisterFoul request = new TimelineRequest.RegisterFoul(
                    10, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    soccerTeamId, soccerPlayerId);

            // when & then
            assertThatThrownBy(() -> timelineService.register(manager, soccerGameId, request))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @DisplayName("타임라인을 삭제할 때")
    @Nested
    class DeleteTest {
        // game 6: 점수 0:0 + QUARTER_START 만 있는 정합 상태 — register 로 쌓아서 replay 검증
        private final Long replayGameId = 6L;
        private final Long replayGameTeamId = 9L;
        private final Long starter1Id = 31L;
        private final Long starter2Id = 32L;
        private final Long candidate1Id = 33L;
        private final Long candidate2Id = 34L;

        @Test
        void 마지막_타임라인을_차례로_삭제한다() {
            // given
            List<Timeline> game1Timelines = timelineFixtureRepository.findAllLatest(gameId);

            // when
            while (!game1Timelines.isEmpty()) {
                Timeline lastTimeline = game1Timelines.get(0);
                timelineService.deleteTimeline(manager, gameId, lastTimeline.getId());
                game1Timelines = timelineFixtureRepository.findAllLatest(gameId);
            }

            // then
            assertThat(timelineFixtureRepository.findAllLatest(gameId)).isEmpty();
        }

        @Test
        void 진행_중_경기의_중간_득점을_삭제하면_점수와_이후_스냅샷이_재계산된다() {
            // given
            registerGoal(starter1Id, null);
            registerGoal(starter2Id, null);
            Timeline firstGoal = oldestScoreTimeline(replayGameId);
            ScoreTimeline secondGoal = (ScoreTimeline) timelineFixtureRepository.findAllLatest(replayGameId).get(0);
            assertThat(secondGoal.getSnapshotScore1() + secondGoal.getSnapshotScore2()).isEqualTo(2);

            // when
            timelineService.deleteTimeline(manager, replayGameId, firstGoal.getId());

            // then
            GameTeam gameTeam = entityUtils.getEntity(replayGameTeamId, GameTeam.class);
            ScoreTimeline remainingGoal = (ScoreTimeline) entityUtils.getEntity(secondGoal.getId(), Timeline.class);
            assertAll(
                    () -> assertThat(gameTeam.getScore()).as("팀 점수").isEqualTo(1),
                    () -> assertThat(remainingGoal.getSnapshotScore1() + remainingGoal.getSnapshotScore2()).as("남은 골의 스냅샷 합").isEqualTo(1)
            );
        }

        @Test
        void 교체로_들어온_선수가_이후_어시스트에_등장하면_교체를_삭제할_수_없다() {
            // given
            registerReplacement(starter1Id, candidate1Id);
            Timeline replacement = timelineFixtureRepository.findAllLatest(replayGameId).get(0);
            registerGoal(starter2Id, candidate1Id);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, replayGameId, replacement.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.REPLACEMENT_PLAYER_HAS_LATER_RECORDS);
        }

        @Test
        void 교체된_선수가_이후_재교체에_등장하면_삭제할_수_없다() {
            // given
            registerReplacement(starter1Id, candidate1Id);
            Timeline firstReplacement = timelineFixtureRepository.findAllLatest(replayGameId).get(0);
            registerReplacement(candidate1Id, candidate2Id);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, replayGameId, firstReplacement.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.REPLACEMENT_PLAYER_HAS_LATER_RECORDS);
        }

        @Test
        void 이후_기록과_무관한_중간_교체는_삭제되고_라인업이_복원된다() {
            // given
            registerReplacement(starter1Id, candidate1Id);
            Timeline replacement = timelineFixtureRepository.findAllLatest(replayGameId).get(0);
            registerGoal(starter2Id, null);

            // when
            timelineService.deleteTimeline(manager, replayGameId, replacement.getId());

            // then
            LineupPlayer origin = entityUtils.getEntity(starter1Id, LineupPlayer.class);
            LineupPlayer replaced = entityUtils.getEntity(candidate1Id, LineupPlayer.class);
            GameTeam gameTeam = entityUtils.getEntity(replayGameTeamId, GameTeam.class);
            assertAll(
                    () -> assertThat(origin.isPlaying()).isTrue(),
                    () -> assertThat(origin.isReplaced()).isFalse(),
                    () -> assertThat(replaced.isPlaying()).isFalse(),
                    () -> assertThat(replaced.isReplaced()).isFalse(),
                    () -> assertThat(gameTeam.getScore()).isEqualTo(1)
            );
        }

        @Test
        void 쿼터_진행_기록은_중간_삭제할_수_없다() {
            // given: game 5 의 가장 오래된 타임라인은 QUARTER_START
            Long basketballGameId = 5L;
            List<Timeline> timelines = timelineFixtureRepository.findAllLatest(basketballGameId);
            Timeline quarterStart = timelines.get(timelines.size() - 1);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, basketballGameId, quarterStart.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.PROGRESS_TIMELINE_NOT_LAST);
        }

        @Test
        void 종료된_경기는_중간_기록을_삭제할_수_없다() {
            // given: game 2 는 FINISHED
            Long finishedGameId = 2L;
            List<Timeline> timelines = timelineFixtureRepository.findAllLatest(finishedGameId);
            Timeline middleTimeline = timelines.get(timelines.size() - 1);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, finishedGameId, middleTimeline.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.MIDDLE_DELETE_ONLY_WHILE_PLAYING);
        }

        @Test
        void 시작_전_경기는_중간_기록을_삭제할_수_없다() {
            // given: game 7 은 SCHEDULED
            Long scheduledGameId = 7L;
            List<Timeline> timelines = timelineFixtureRepository.findAllLatest(scheduledGameId);
            Timeline middleTimeline = timelines.get(timelines.size() - 1);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, scheduledGameId, middleTimeline.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.MIDDLE_DELETE_ONLY_WHILE_PLAYING);
        }

        @Test
        void 진행_중_경기에_경기_종료_기록이_섞여_있으면_중간_삭제가_거부된다() {
            // given: game 1 은 PLAYING 인데 GAME_END 타임라인이 존재하는 비정합 상태
            Timeline middleScore = oldestScoreTimeline(gameId);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, gameId, middleScore.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.INCONSISTENT_PROGRESS_STATE);
        }

        @Test
        void 다른_경기의_타임라인은_삭제할_수_없다() {
            // given
            Timeline game1Timeline = timelineFixtureRepository.findAllLatest(gameId).get(0);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, 5L, game1Timeline.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.TIMELINE_NOT_FOUND);
        }

        @Test
        void 존재하지_않는_타임라인은_삭제할_수_없다() {
            assertThatThrownBy(() -> timelineService.deleteTimeline(manager, gameId, 99999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(TimelineErrorMessage.TIMELINE_NOT_FOUND);
        }

        @Test
        void 경기의_매니저가_아닌_회원은_타임라인을_삭제할_수_없다() {
            // given
            Member nonManager = entityUtils.getEntity(2L, Member.class);
            Timeline lastTimeline = timelineFixtureRepository.findAllLatest(gameId).get(0);

            // when & then
            assertThatThrownBy(() -> timelineService.deleteTimeline(nonManager, gameId, lastTimeline.getId()))
                    .isInstanceOf(UnauthorizedException.class);
        }

        private void registerGoal(Long scorerLineupPlayerId, Long assistLineupPlayerId) {
            timelineService.register(manager, replayGameId, new TimelineRequest.RegisterSoccerScore(
                    replayGameTeamId, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    scorerLineupPlayerId, 10, assistLineupPlayerId));
        }

        private void registerReplacement(Long originLineupPlayerId, Long replacementLineupPlayerId) {
            timelineService.register(manager, replayGameId, new TimelineRequest.RegisterReplacement(
                    replayGameTeamId, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    originLineupPlayerId, replacementLineupPlayerId, 20, null));
        }

        private Timeline oldestScoreTimeline(Long targetGameId) {
            List<Timeline> timelines = timelineFixtureRepository.findAllLatest(targetGameId);
            for (int i = timelines.size() - 1; i >= 0; i--) {
                if (timelines.get(i) instanceof ScoreTimeline) {
                    return timelines.get(i);
                }
            }
            throw new IllegalStateException("득점 타임라인이 없습니다: gameId=" + targetGameId);
        }
    }

    @Test
    void 경기_종료_후_타임라인을_등록하려고_하면_에러가_발생한다() {
        // given
        Long team1Id = 1L;
        Long team1PlayerId = 1L;
        Long finishedGameId = 2L;

        TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                team1PlayerId, 3, null);

        // when & then
        assertThatThrownBy(() -> timelineService.register(manager, finishedGameId, request)).isInstanceOf(
                CustomException.class).hasMessage(TimelineErrorMessage.GAME_ALREADY_FINISHED);
    }

    @DisplayName("동시성 테스트: Game 상태 확인 및 점수 갱신 직렬화")
    @Nested
    class ConcurrencyTest {

        public static final int SIZE_OF_SAVED_TIMELINE_DATA = 14;
        private final int numberOfAttempts = 10;
        private final ExecutorService executorService = Executors.newFixedThreadPool(numberOfAttempts);

        // 2025-10-06 h2 환경에서는 데드락 발생하기에 무효화
        // ci 를 profile 로 두고 실행 시 통과

        @Disabled
        @Test
        void 여러_스레드에서_동시에_득점_타임라인을_등록하면_모두_성공하고_점수가_누락되지_않아야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterSoccerScore request = new TimelineRequest.RegisterSoccerScore(1L, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), 1L, 1, null);

            int initialScore1 = 15;
            int initialScore2 = 10;
            int expectedFinalScore1 = initialScore1 + numberOfAttempts;

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 PESSIMISTIC_WRITE 락에 의해 직렬화되어 성공해야 함")
                    .isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);

            // 3. 최종 점수 스냅샷 확인
            ScoreTimeline lastTimeline = (ScoreTimeline) actualTimelines.get(0);

            assertAll(() -> assertThat(lastTimeline.getSnapshotScore1()).as("팀1 최종 스냅샷 점수: %d", expectedFinalScore1)
                            .isEqualTo(expectedFinalScore1),
                    () -> assertThat(lastTimeline.getSnapshotScore2()).as("팀2 최종 스냅샷 점수").isEqualTo(initialScore2));
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_교체_타임라인을_등록하면_모두_성공하고_선수_상태가_일관되어야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);
            Long team1Id = 1L;
            // 1번 선수가 2번 선수로 교체되는 요청을 여러 번 시도
            Long originPlayerId = 1L;
            Long replacedPlayerId = 2L;

            TimelineRequest.RegisterReplacement request = new TimelineRequest.RegisterReplacement(team1Id, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), originPlayerId, replacedPlayerId, 1, null);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            // 교체가 이미 이루어진 후 또다시 동일한 교체 요청이 들어오면 실패할 수 있음.
                            // 여기서는 주로 락으로 인한 데드락/롤백 여부를 확인.
                            System.err.println("교체 타임라인 등록 중 예외 발생: " + e.getMessage());
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 최종 생성된 타임라인 수 확인
            // 교체 요청은 한 번만 성공하고, 이후 요청은 LineupPlayer 상태 때문에 실패해야 하므로,
            // 성공 카운트가 1에 가까운지, 그리고 예상치 못한 데드락이 없었는지 확인
            System.out.println("성공한 교체 타임라인 등록 횟수: " + successCount.get());
            assertThat(successCount.get()).as("LineupPlayer 상태 변경이 직렬화되어 한 번만 성공해야 함")
                    .isGreaterThanOrEqualTo(1); // 최소 한 번은 성공해야 함

            // 2. 최종 LineupPlayer 상태 확인
            ReplacementTimeline lastReplacement = timelineFixtureRepository.findReplacementTimelineWithLineupPlayers(
                            gameId).stream().max((t1, t2) -> (int) (t1.getId() - t2.getId()))
                    .orElseThrow(() -> new AssertionError("교체 타임라인이 생성되지 않았습니다."));

            assertAll(() -> assertThat(lastReplacement.getOriginLineupPlayer().isPlaying()).as(
                            "Origin Player는 최종적으로 Inactive 상태여야 함").isEqualTo(false),
                    () -> assertThat(lastReplacement.getReplacedLineupPlayer().isPlaying()).as(
                            "Replaced Player는 최종적으로 Active 상태여야 함").isEqualTo(true));
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_게임_진행_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterProgress request = new TimelineRequest.RegisterProgress(10, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                    GameProgressType.QUARTER_START);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("게임 진행 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            // GameProgressTimeline은 상태 변경을 유발하지만, 점수 갱신 같은 Race Condition 위험이 적어 모두 성공할 것으로 기대
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(GameProgressTimeline.class);
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_승부차기_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterPk request = new TimelineRequest.RegisterPk(1, SportType.SOCCER, SoccerQuarter.PENALTY_SHOOTOUT.name(), 1L,
                    // teamId
                    1L, // playerId
                    true // isSuccess
            );

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("승부차기 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(PKTimeline.class);
        }

        @Disabled
        @Test
        void 여러_스레드에서_동시에_경고_타임라인을_등록하면_모두_성공해야_한다() throws Exception {
            // given
            AtomicInteger successCount = new AtomicInteger(0);

            TimelineRequest.RegisterWarningCard request = new TimelineRequest.RegisterWarningCard(1, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(), 1L, // teamId
                    1L, // playerId
                    WarningCardType.YELLOW);

            // when
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfAttempts)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            timelineService.register(manager, gameId, request);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("경고 타임라인 등록 중 예외 발생: " + e.getMessage(), e);
                        }
                    }, executorService)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // then
            // 1. 모든 요청이 성공적으로 처리되었는지 확인
            assertThat(successCount.get()).as("모든 요청은 성공해야 함").isEqualTo(numberOfAttempts);

            // 2. 최종 생성된 타임라인 수 확인
            List<Timeline> actualTimelines = timelineFixtureRepository.findAllLatest(gameId);
            assertThat(actualTimelines).hasSize(numberOfAttempts + SIZE_OF_SAVED_TIMELINE_DATA);
            assertThat(actualTimelines.get(0)).isInstanceOf(WarningCardTimeline.class);
        }
    }
}