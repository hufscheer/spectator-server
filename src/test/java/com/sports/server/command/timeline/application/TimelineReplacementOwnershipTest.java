package com.sports.server.command.timeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.game.domain.LineupPlayer;
import com.sports.server.command.game.exception.GameErrorMessages;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.timeline.dto.TimelineRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/timeline-fixture.sql")
@DisplayName("축구 교체 타임라인은 그 경기 선수만 쓸 수 있다")
class TimelineReplacementOwnershipTest extends ServiceTest {

    // 경기 1의 선수는 1~10, 다른 경기(3)의 선수는 13~16
    private static final Long GAME_ID = 1L;

    @Autowired
    private TimelineService timelineService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member manager;

    @BeforeEach
    void setUp() {
        manager = memberRepository.findMemberByEmail("john.doe@example.com").orElseThrow();
        jdbcTemplate.update("UPDATE lineup_players SET is_playing = true WHERE id = 13");
    }

    private TimelineRequest.RegisterReplacement replacement(Long origin, Long replaced) {
        return new TimelineRequest.RegisterReplacement(1L, SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(),
                origin, replaced, 10, null);
    }

    @Test
    void 다른_경기_선수끼리_교체하면_400이고_선수_상태는_그대로다() {
        assertThatThrownBy(() -> timelineService.register(manager, GAME_ID, replacement(13L, 14L)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.LINEUP_PLAYER_NOT_IN_GAME_TEAM_EXCEPTION);

        LineupPlayer origin = entityUtils.getEntity(13L, LineupPlayer.class);
        LineupPlayer replaced = entityUtils.getEntity(14L, LineupPlayer.class);
        assertThat(origin.isPlaying()).isTrue();
        assertThat(replaced.isPlaying()).isFalse();
        assertThat(origin.getReplacedPlayer()).isNull();
        assertThat(replaced.getReplacedPlayer()).isNull();
    }

    @Test
    void 교체_대상이_다른_경기_선수이면_400이다() {
        assertThatThrownBy(() -> timelineService.register(manager, GAME_ID, replacement(1L, 14L)))
                .isInstanceOf(BadRequestException.class);
        assertThat(entityUtils.getEntity(1L, LineupPlayer.class).isPlaying()).isTrue();
        assertThat(entityUtils.getEntity(14L, LineupPlayer.class).isPlaying()).isFalse();
    }

    // 경기 5는 농구 경기다. 선수 17은 뛰는 중, 21은 후보다
    @Test
    void 농구_경기에_본문_종목이_축구인_교체를_보내면_400이고_선수_상태는_그대로다() {
        TimelineRequest.RegisterReplacement soccerBody = new TimelineRequest.RegisterReplacement(7L,
                SportType.SOCCER, SoccerQuarter.FIRST_HALF.name(), 17L, 21L, 10, null);
        boolean originPlaying = entityUtils.getEntity(17L, LineupPlayer.class).isPlaying();
        boolean replacementPlaying = entityUtils.getEntity(21L, LineupPlayer.class).isPlaying();

        assertThatThrownBy(() -> timelineService.register(manager, 5L, soccerBody))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(GameErrorMessages.TIMELINE_SPORT_TYPE_MISMATCH_EXCEPTION);

        assertThat(entityUtils.getEntity(17L, LineupPlayer.class).isPlaying()).isEqualTo(originPlaying);
        assertThat(entityUtils.getEntity(21L, LineupPlayer.class).isPlaying()).isEqualTo(replacementPlaying);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM timelines WHERE game_id = 5 AND type LIKE '%REPLACEMENT%'", Long.class))
                .isZero();
    }
}
