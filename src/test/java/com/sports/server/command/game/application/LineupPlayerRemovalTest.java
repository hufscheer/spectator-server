package com.sports.server.command.game.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.command.game.domain.LineupPlayerRepository;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.TimelineFixtureRepository;
import com.sports.server.command.timeline.application.TimelineService;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/timeline-fixture.sql")
@DisplayName("라인업에서 선수를 뺄 때")
class LineupPlayerRemovalTest extends ServiceTest {

    private static final Long GAME_ID = 6L;
    private static final Long GAME_TEAM_ID = 9L;
    private static final Long STARTER_1 = 31L;
    private static final Long STARTER_2 = 32L;
    private static final Long CANDIDATE_1 = 33L;
    private static final Long CANDIDATE_2 = 34L;

    @Autowired
    private LineupPlayerService lineupPlayerService;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LineupPlayerRepository lineupPlayerRepository;

    @Autowired
    private TimelineFixtureRepository timelineFixtureRepository;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
    }

    @Test
    void 기록이_없으면_뺄_수_있다() {
        // when
        lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, CANDIDATE_2);

        // then
        assertThat(lineupPlayerRepository.findById(CANDIDATE_2)).isEmpty();
    }

    @Test
    void 득점한_선수는_뺄_수_없다() {
        // given
        registerGoal(STARTER_1, null);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, STARTER_1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.LINEUP_PLAYER_HAS_RECORDS);
    }

    @Test
    void 어시스트만_한_선수도_뺄_수_없다() {
        // given: 어시스트는 교체 삭제 판정에서만 제외되는 것이고, 참조는 그대로 남는다
        registerGoal(STARTER_1, CANDIDATE_1);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, CANDIDATE_1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.LINEUP_PLAYER_HAS_RECORDS);
    }

    @Test
    void 교체로_나간_선수도_뺄_수_없다() {
        // given
        registerReplacement(STARTER_1, CANDIDATE_1);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, STARTER_1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.LINEUP_PLAYER_HAS_RECORDS);
    }

    @Test
    void 교체로_들어온_선수도_뺄_수_없다() {
        // given
        registerReplacement(STARTER_1, CANDIDATE_1);

        // when & then
        assertThatThrownBy(() -> lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, CANDIDATE_1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.LINEUP_PLAYER_HAS_RECORDS);
    }

    @Test
    void 기록을_지우고_나면_뺄_수_있다() {
        // given
        registerGoal(STARTER_2, null);
        Long goalId = timelineFixtureRepository.findAllLatest(GAME_ID).get(0).getId();
        timelineService.deleteTimeline(manager, GAME_ID, goalId);

        // when
        lineupPlayerService.removePlayerFromLineup(GAME_TEAM_ID, STARTER_2);

        // then
        assertThat(lineupPlayerRepository.findById(STARTER_2)).isEmpty();
    }

    private void registerGoal(Long scorerId, Long assistId) {
        timelineService.register(manager, GAME_ID, new TimelineRequest.RegisterSoccerScore(
                GAME_TEAM_ID, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                scorerId, 10, assistId, null));
    }

    private void registerReplacement(Long originId, Long replacementId) {
        timelineService.register(manager, GAME_ID, new TimelineRequest.RegisterReplacement(
                GAME_TEAM_ID, SportType.SOCCER, SoccerQuarter.SECOND_HALF.name(),
                originId, replacementId, 20, null));
    }
}
