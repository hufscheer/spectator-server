package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.query.dto.response.GameTimelineResponse;
import com.sports.server.query.dto.response.LeagueResponseWithGames;
import com.sports.server.query.dto.response.LeagueResponseWithInProgressGames;
import com.sports.server.query.dto.response.RecordResponse;
import com.sports.server.query.dto.response.ScoreRecordResponse;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * 팀은 소프트 삭제라 game_teams 가 지워진 팀을 계속 가리킨다. 그 팀을 지연 로딩하면 @Where 에 걸려
 * EntityNotFoundException 500 이 났다 (운영 대회 224 → 경기 475 → 팀 172).
 */
@DisplayName("지워진 팀을 가리키는 경기를 조회할 때")
class DeletedTeamReferenceQueryTest extends ServiceTest {

    @Autowired
    private LeagueQueryService leagueQueryService;

    @Autowired
    private TimelineQueryService timelineQueryService;

    @Autowired
    private EntityUtils entityUtils;

    /**
     * 팀A(1)를 지운다. 1번 경기(진행 중, 팀A·팀B)가 지워진 팀을 가리킨다.
     */
    @Nested
    @Sql(scripts = "/timeline-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 1")
    @DisplayName("대회 경기 목록과 타임라인은")
    class LeagueGamesAndTimeline {

        private static final long DELETED_TEAM_GAME_TEAM_ID = 1L;

        @Test
        void 대회_경기_목록은_지워진_팀을_빼고_내려준다() {
            // when
            LeagueResponseWithGames response = leagueQueryService.findLeagueAndGames(1L);

            // then
            assertThat(response.playingGames())
                    .filteredOn(game -> game.id().equals(1L))
                    .singleElement()
                    .satisfies(game -> assertThat(game.gameTeams())
                            .extracting(LeagueResponseWithGames.GameDetail.GameTeam::gameTeamName)
                            .containsExactly("팀B"));
        }

        @Test
        void 타임라인은_지워진_팀의_기록도_팀_정보만_비우고_내려준다() {
            // when
            GameTimelineResponse response = timelineQueryService.getTimelines(1L);

            // then
            List<RecordResponse> records = response.timelines().stream()
                    .flatMap(timeline -> timeline.records().stream())
                    .toList();
            assertThat(records)
                    .filteredOn(record -> Objects.equals(record.gameTeamId(), DELETED_TEAM_GAME_TEAM_ID))
                    .isNotEmpty()
                    .allSatisfy(record -> {
                        assertThat(record.teamName()).isNull();
                        assertThat(record.teamImageUrl()).isNull();
                    });

            // 점수 스냅샷은 두 팀 칸을 그대로 두고 점수만 쓴다 (매니저 화면이 snapshot[0], snapshot[1] 을 읽는다)
            List<ScoreRecordResponse> scoreRecords = records.stream()
                    .map(RecordResponse::scoreRecord)
                    .filter(Objects::nonNull)
                    .toList();
            assertThat(scoreRecords).isNotEmpty()
                    .allSatisfy(scoreRecord -> assertThat(scoreRecord.snapshot())
                            .hasSize(2)
                            .extracting(ScoreRecordResponse.Snapshot::teamName)
                            .containsExactly(null, "팀B"));
        }
    }

    /**
     * 2번 경기(종료)는 팀A 가 이겼다. 승리 팀 조회는 원래 팀을 inner join 해서 지워진 팀이면 비어 나온다.
     * 픽스처의 2번 경기 득점 기록은 1번 경기 선수를 득점자로 가리키는데, API 로는 생길 수 없는 데이터라
     * (Game.findTeamOf 가 막는다) 승리 팀 경로만 보도록 기록을 지운다.
     */
    @Nested
    @DisplayName("종료 경기는")
    @Sql(scripts = "/timeline-fixture.sql", statements = {
            "UPDATE teams SET is_deleted = 1 WHERE id = 1",
            "DELETE FROM timelines WHERE game_id = 2"
    })
    class FinishedGame {

        @Test
        void 이긴_팀이_지워졌으면_승리_팀_없이_내려준다() {
            // when
            GameTimelineResponse response = timelineQueryService.getTimelines(2L);

            // then
            assertThat(response.winner()).isNull();
        }
    }

    /**
     * 서어 뻬데뻬(2)를 지운다. 1번 대회의 진행 중 경기(경영 야생마·서어 뻬데뻬)가 지워진 팀을 가리킨다.
     */
    @Nested
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 2")
    @DisplayName("매니저 홈은")
    class ManagerHome {

        @Test
        void 진행_중_경기는_지워진_팀을_빼고_내려준다() {
            // given
            Member manager = entityUtils.getEntity(1L, Member.class);

            // when
            List<LeagueResponseWithInProgressGames> response = leagueQueryService.findLeaguesByManager(manager);

            // then
            assertThat(response)
                    .filteredOn(league -> league.id().equals(1L))
                    .singleElement()
                    .satisfies(league -> assertThat(league.inProgressGames())
                            .flatExtracting(LeagueResponseWithInProgressGames.GameDetailResponse::gameTeams)
                            .extracting(LeagueResponseWithInProgressGames.GameDetailResponse.GameTeamResponse::gameTeamName)
                            .containsExactly("경영 야생마"));
        }
    }
}
