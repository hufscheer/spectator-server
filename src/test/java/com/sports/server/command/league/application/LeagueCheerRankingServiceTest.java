package com.sports.server.command.league.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.league.domain.LeagueStatistics;
import com.sports.server.command.league.domain.LeagueStatisticsRepository;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql("/league-fixture.sql")
@DisplayName("리그의 총 응원수와 응원톡 수를 다시 셀 때")
class LeagueCheerRankingServiceTest extends ServiceTest {

    @Autowired
    private LeagueCheerRankingService leagueCheerRankingService;

    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Autowired
    private LeagueStatisticsRepository leagueStatisticsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 종료된_경기의_응원수와_활성_응원톡만_센다() {
        // given 픽스처의 리그1 경기1(팀1 150, 팀2 100)은 PLAYING, 경기2(팀3 75)는 SCHEDULED 라서 종료로 바꾼다
        jdbcTemplate.update("UPDATE games SET state = 'FINISHED' WHERE id IN (1, 2)");

        // when
        leagueCheerRankingService.updateCheerRankingsForLeague(1L);

        // then 응원톡: 팀1 2, 팀2 2, 팀3 1(차단 1건 제외)
        assertTotals(1L, 1L, 150, 2);
        assertTotals(1L, 2L, 100, 2);
        assertTotals(1L, 3L, 75, 1);
    }

    @Test
    void 끝나지_않은_경기는_세지_않아_0으로_돌아간다() {
        // when 경기1 PLAYING, 경기2 SCHEDULED
        leagueCheerRankingService.updateCheerRankingsForLeague(1L);

        // then 픽스처에 미리 들어 있던 합계(150 등)도 종료 경기 기준으로 다시 써진다
        assertTotals(1L, 1L, 0, 0);
        assertTotals(1L, 2L, 0, 0);
        assertTotals(1L, 3L, 0, 0);
    }

    @Test
    void 응원수나_응원톡이_없는_팀은_0으로_설정된다() {
        // given 리그2 경기3(FINISHED): 팀4 30 응원 1 응원톡, 팀5 0 / 0
        // when
        leagueCheerRankingService.updateCheerRankingsForLeague(2L);

        // then
        assertTotals(2L, 4L, 30, 1);
        assertTotals(2L, 5L, 0, 0);
    }

    @Test
    void 지워진_팀이_있어도_다시_세고_최다_팀은_남은_팀에서_고른다() {
        // given 경기1, 2 를 종료로 바꾸고 팀1(응원 150 으로 1위였을 팀)을 지운다
        jdbcTemplate.update("UPDATE games SET state = 'FINISHED' WHERE id IN (1, 2)");
        jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 1");

        // when
        leagueCheerRankingService.updateCheerRankingsForLeague(1L);

        // then 합계는 리그팀 행마다 FK 로 쓰인다(league_teams id 1~3 = 팀 1~3)
        assertThat(totalsOfLeagueTeam(1L)).containsExactly(150, 2);
        assertThat(totalsOfLeagueTeam(2L)).containsExactly(100, 2);
        assertThat(totalsOfLeagueTeam(3L)).containsExactly(75, 1);
        // 최다 응원, 응원톡 팀은 지워진 팀1 을 빼고 고른다
        LeagueStatistics statistics = leagueStatisticsRepository.findByLeagueId(1L);
        assertThat(statistics.getMostCheeredTeam().getId()).isEqualTo(2L);
        assertThat(statistics.getMostCheerTalksTeam().getId()).isEqualTo(2L);
    }

    @Test
    void 지워진_대회면_NotFound_를_던진다() {
        // 핸들러가 잡아 ERROR 로그로 남긴다(득점왕 재집계와 같은 동작)
        assertThatThrownBy(() -> leagueCheerRankingService.updateCheerRankingsForLeague(10L))
                .isInstanceOf(NotFoundException.class);
    }

    private List<Integer> totalsOfLeagueTeam(Long leagueTeamId) {
        return jdbcTemplate.queryForObject(
                "SELECT total_cheer_count, total_talk_count FROM league_teams WHERE id = ?",
                (rs, i) -> List.of(rs.getInt(1), rs.getInt(2)), leagueTeamId);
    }

    private void assertTotals(Long leagueId, Long teamId, int cheerCount, int talkCount) {
        List<LeagueTeam> leagueTeams = leagueTeamRepository.findByLeagueId(leagueId);
        LeagueTeam leagueTeam = leagueTeams.stream()
                .filter(lt -> lt.getTeam().getId().equals(teamId))
                .findFirst().orElseThrow();
        assertThat(leagueTeam.getTotalCheerCount()).isEqualTo(cheerCount);
        assertThat(leagueTeam.getTotalTalkCount()).isEqualTo(talkCount);
    }
}
