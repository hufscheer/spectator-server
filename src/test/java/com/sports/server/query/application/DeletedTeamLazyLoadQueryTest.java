package com.sports.server.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.dto.PageRequestDto;
import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.query.dto.response.BracketResponse;
import com.sports.server.query.support.StudentNumberDisclosure;
import com.sports.server.query.support.StudentNumberDisclosurePolicy;
import com.sports.server.query.repository.BracketMatchQueryRepository;
import com.sports.server.query.repository.TeamQueryRepository;
import com.sports.server.support.ServiceTest;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * 재현 전용. 소프트 삭제된 팀(teams.is_deleted = 1)을 team_players, league_teams, game_teams, bracket_matches 가
 * 계속 가리킬 때 조회 API 가 500 을 내는지 본다. 지금은 예외 없이 끝나는지만 단언한다.
 */
@DisplayName("지워진 팀을 지연 로딩하는 조회는")
class DeletedTeamLazyLoadQueryTest extends ServiceTest {

    @Autowired
    private LeagueQueryService leagueQueryService;

    @Autowired
    private PlayerQueryService playerQueryService;

    @Autowired
    private BracketQueryService bracketQueryService;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private TeamQueryRepository teamQueryRepository;

    @Autowired
    private BracketMatchQueryRepository bracketMatchQueryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * league-fixture: 선수 1, 2 는 팀 3 소속(team_players 3, 4), 팀 3 은 대회 1 의 리그팀 3, 대회 1(2024) 득점왕 1, 2.
     * 지점 1\~4 는 main 코드를 고치지 않고 현재 동작을 남긴다.
     */
    @Nested
    @DisplayName("지점 1. 득점왕 요약(GET /leagues/recent-summary)은")
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 3")
    class RecentSummaryUnitOfTopScorer {

        @Test
        void 득점왕의_최근_팀이_지워지면_단과대는_빈_문자열이고_500은_아니다() {
            var topScorers = leagueQueryService.findRecentSummary(2024, null, null, 0, 5).topScorers();

            assertThat(topScorers)
                    .filteredOn(scorer -> scorer.playerName().equals("진승희") || scorer.playerName().equals("이동규"))
                    .hasSize(2)
                    .allSatisfy(scorer -> assertThat(scorer.unit()).isEmpty());
            assertThat(topScorers)
                    .filteredOn(scorer -> scorer.playerName().equals("이현제"))
                    .singleElement()
                    .satisfies(scorer -> assertThat(scorer.unit()).isEqualTo("경영대학"));
        }
    }

    @Nested
    @DisplayName("지점 2. 리그팀 선수 목록(GET /leagues/teams/{leagueTeamId}/players)은")
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 3")
    class PlayersByLeagueTeam {

        @Test
        void 리그팀의_팀이_지워져도_예외_없이_끝난다() {
            // 리그팀 3 = 대회 1 의 팀 3
            assertThatCode(() -> leagueQueryService.findPlayersByLeagueTeam(3L, anonymous()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("지점 3a. 대회 팀 목록(GET /leagues/{leagueId}/teams)은")
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 3")
    class TeamsByLeagueRound {

        @Test
        void 지워진_팀은_목록에서_빠진다() {
            assertThat(leagueQueryService.findTeamsByLeagueRound(1L, null, null))
                    .extracting(team -> team.teamId())
                    .containsExactlyInAnyOrder(1L, 2L);
        }
    }

    /**
     * league-fixture 의 대회 1 통계: 우승 1, 준우승 2, 최다 응원 1, 최다 응원톡 2.
     */
    @Nested
    @DisplayName("지점 3b. 대회 통계(GET /leagues/{leagueId}/statistics, ofWithCheerCount/ofWithTotalTalkCount)는")
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id IN (1, 2)")
    class LeagueStatistic {

        @Test
        void 통계의_팀이_지워져도_예외_없이_끝난다() {
            assertThatCode(() -> leagueQueryService.findLeagueStatistic(1L))
                    .doesNotThrowAnyException();
        }
    }

    /**
     * league-fixture: 선수 1, 2 는 팀 3, 선수 3, 4 는 팀 1. 회원 1 은 조직 1.
     */
    @Nested
    @DisplayName("지점 4. 선수 조회는")
    @Sql(scripts = "/league-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 3")
    class PlayerQuery {

        @Test
        void 선수_목록_GET_players_은_지워진_팀을_teams_에서_뺀다() {
            Member member = entityUtils.getEntity(1L, Member.class);

            var content = playerQueryService.getAllPlayers(member, new PageRequestDto(null, 20), null, null).content();

            assertThat(content)
                    .filteredOn(player -> player.playerId() == 1L || player.playerId() == 2L)
                    .hasSize(2)
                    .allSatisfy(player -> assertThat(player.teams()).isEmpty());
            assertThat(content)
                    .filteredOn(player -> player.playerId() == 3L)
                    .singleElement()
                    .satisfies(player -> assertThat(player.teams()).hasSize(1));
        }

        @Test
        void 선수_상세_GET_players_playerId_는_지워진_팀을_teams_에서_뺀다() {
            assertThat(playerQueryService.getPlayerDetail(1L, anonymous()).teams()).isEmpty();
        }
    }

    /**
     * bracket-fixture: 대회 2 는 4강 1경기가 팀 1·2(경기 미연결), 대회 3 의 4강 1경기(경기 2)는 팀 5(승)·6(패),
     * 대회 4 는 4강이 모두 끝났고 승자 1·3, 패자 2·4, 대회 6 은 3·4위전 경기(팀 2·4)까지 만들어졌다.
     * 지워진 팀도 저장된 이름과 로고로 지워지지 않은 팀과 똑같이 보여야 한다.
     */
    @Nested
    @DisplayName("지점 5. 대진표(GET /leagues/{leagueId}/bracket)는")
    class Bracket {

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 1")
        void 첫_라운드_슬롯의_지워진_팀을_저장된_이름과_로고로_보여준다() {
            BracketResponse response = bracketQueryService.findBracketByLeagueId(2L);

            BracketResponse.MatchResponse match = match(response, 4, 1);
            assertTeam(match.team1(), 1L, "경영 야생마", "https://example.com/logos/1.png");
            assertTeam(match.team2(), 2L, "서어 뻬데뻬", "https://example.com/logos/2.png");
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 5")
        void 경기가_연결된_매치의_지워진_팀을_보여주고_승자로_판정한다() {
            // 대회 3 의 4강 1경기(경기 2)는 팀 5(승)·6(패). 결승 슬롯은 승자를 읽는다
            BracketResponse response = bracketQueryService.findBracketByLeagueId(3L);

            BracketResponse.MatchResponse semiFinal = match(response, 4, 1);
            assertTeam(semiFinal.team1(), 5L, "컴공 독수리", "https://example.com/logos/5.png");
            assertTeam(semiFinal.team2(), 6L, "전자 번개", "https://example.com/logos/6.png");
            assertThat(semiFinal.winnerTeamId()).isEqualTo(5L);
            assertTeam(match(response, 2, 1).team1(), 5L, "컴공 독수리", "https://example.com/logos/5.png");
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 1")
        void 준결승_승자가_지워져도_결승_슬롯에_보여준다() {
            // 대회 4 의 4강 1경기(경기 6)는 팀 1(승)·2(패)
            BracketResponse response = bracketQueryService.findBracketByLeagueId(4L);

            assertThat(match(response, 4, 1).winnerTeamId()).isEqualTo(1L);
            assertTeam(match(response, 2, 1).team1(), 1L, "경영 야생마", "https://example.com/logos/1.png");
            assertTeam(match(response, 2, 1).team2(), 3L, "미컴 축구생각", "https://example.com/logos/3.png");
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 2")
        void 준결승_패자가_지워져도_3_4위전_슬롯에_보여준다() {
            // 대회 4 는 3·4위전 경기가 없어 thirdPlaceSlotOf 가 슬롯을 채운다: 패자 2·4
            BracketResponse.MatchResponse thirdPlace = bracketQueryService.findBracketByLeagueId(4L).thirdPlaceMatch();

            assertTeam(thirdPlace.team1(), 2L, "서어 뻬데뻬", "https://example.com/logos/2.png");
            assertTeam(thirdPlace.team2(), 4L, "체교 불사조", "https://example.com/logos/4.png");
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id = 2")
        void 삼사위전_경기가_연결된_매치의_지워진_팀도_보여준다() {
            // 대회 6 의 3·4위전 경기(경기 12)는 팀 2·4
            BracketResponse.MatchResponse thirdPlace = bracketQueryService.findBracketByLeagueId(6L).thirdPlaceMatch();

            assertTeam(thirdPlace.team1(), 2L, "서어 뻬데뻬", "https://example.com/logos/2.png");
            assertTeam(thirdPlace.team2(), 4L, "체교 불사조", "https://example.com/logos/4.png");
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql", statements = "UPDATE teams SET is_deleted = 1 WHERE id IN (1, 2, 3, 4)")
        void 팀_참조가_프록시가_아니라_Team_인스턴스로_풀리고_팀을_추가로_조회하지_않는다() {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics statistics = sessionFactory.getStatistics();
            statistics.setStatisticsEnabled(true);

            transactionTemplate.executeWithoutResult(status -> {
                teamQueryRepository.findAllInBracketIncludingDeleted(6L);
                statistics.clear();

                List<BracketMatch> matches = bracketMatchQueryRepository.findAllByLeagueIdWithTeamsAndGames(6L);

                BracketMatch first = matches.stream().filter(m -> m.getTeam1() != null).findFirst().orElseThrow();
                assertThat(first.getTeam1().getClass()).isEqualTo(Team.class);
                matches.stream()
                        .filter(match -> match.getGame() != null)
                        .flatMap(match -> match.getGame().getGameTeams().stream())
                        .forEach(gameTeam -> assertThat(gameTeam.getTeam().getClass()).isEqualTo(Team.class));
                assertThat(statistics.getEntityStatistics(Team.class.getName()).getFetchCount()).isZero();
                assertThat(statistics.getEntityStatistics(Team.class.getName()).getLoadCount()).isZero();
                assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
            });
            statistics.setStatisticsEnabled(false);
        }

        @Test
        @Sql(scripts = "/bracket-fixture.sql")
        void 대조_팀이_안_지워졌으면_같은_응답이_나온다() {
            BracketResponse.MatchResponse thirdPlace = bracketQueryService.findBracketByLeagueId(4L).thirdPlaceMatch();

            assertTeam(thirdPlace.team1(), 2L, "서어 뻬데뻬", "https://example.com/logos/2.png");
        }

        private BracketResponse.MatchResponse match(final BracketResponse response, final int round, final int number) {
            return response.rounds().stream()
                    .filter(r -> r.round() == round)
                    .flatMap(r -> r.matches().stream())
                    .filter(m -> m.matchNumber() == number)
                    .findFirst()
                    .orElseThrow();
        }

        private void assertTeam(final BracketResponse.TeamResponse team, final Long id, final String name,
                                final String logo) {
            assertThat(team).isNotNull();
            assertThat(team.teamId()).isEqualTo(id);
            assertThat(team.name()).isEqualTo(name);
            assertThat(team.logoImageUrl()).isEqualTo(logo);
        }
    }

    // 학번 공개 여부와 무관한 테스트라 비로그인으로 부른다
    private static StudentNumberDisclosure anonymous() {
        return new StudentNumberDisclosurePolicy(null).forViewer(Optional.empty());
    }
}
