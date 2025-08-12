//package com.sports.server.command.team.application;
//
//import com.sports.server.command.league.domain.League;
//import com.sports.server.command.member.domain.Member;
//import com.sports.server.command.team.domain.TeamRepository;
//import com.sports.server.command.team.dto.TeamRequest;
//import com.sports.server.common.application.EntityUtils;
//import com.sports.server.common.application.S3Service;
//import com.sports.server.common.exception.UnauthorizedException;
//import com.sports.server.support.ServiceTest;
//import com.sports.server.support.fixture.LeagueTeamPlayerFixtureRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.Assert.assertThrows;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doNothing;
//
//@ExtendWith(MockitoExtension.class)
//public class TeamServiceTest extends ServiceTest {
//
//    @Value("${image.origin-prefix}")
//    private String originPrefix;
//
//    @Value("${image.replaced-prefix}")
//    private String replacePrefix;
//
//    @MockBean
//    private S3Service s3Service;
//
//    @Autowired
//    private EntityUtils entityUtils;
//
//    @Autowired
//    private TeamService leagueTeamService;
//
//    @Autowired
//    private TeamRepository teamRepository;
//
////    @Autowired
////    private LeagueTeamPlayerFixtureRepository leagueTeamPlayerFixtureRepository;
//
//    private String imageUrl;
//
//    private Member manager;
//
//    @BeforeEach
//    void setUp() {
//        manager = entityUtils.getEntity(1L, Member.class);
//        imageUrl = originPrefix + "image_url.png";
//    }
//
//    // TODO: 팀 생성은 선수와 독립
//    // TODO: 팀에 매니저 인증 권한 빼야함
//    @Test
//    void 팀이_정상적으로_등록된다() {
//        // given
//
//
//        TeamRequest.Register request = new TeamRequest.Register(teamName, imageUrl, "color code");
//        doNothing().when(s3Service).doesFileExist(anyString());
//
//        // when
//        TeamService.register(request);
//
//        // then
//        Optional<Team> savedLeagueTeamOptional = teamRepository.findByLeagueAndName(league,
//                leagueTeamName);
//        assertTrue(savedLeagueTeamOptional.isPresent(), "팀이 저장되지 않았습니다.");
//
//        Team savedLeagueTeam = savedLeagueTeamOptional.get();
//        assertEquals(leagueTeamName, savedLeagueTeam.getName());
//    }
//
//    @Test
//    void origin_prefix가_포함되지_않은_이미지_url을_등록할_경우_예외가_발생한다() {
//        // given
//        Long leagueId = 1L;
//        Member manager = entityUtils.getEntity(1L, Member.class);
//        String leagueTeamName = "name";
//        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                new LeagueTeamPlayerRequest.Register("name-a", 1, "2020033320"),
//                new LeagueTeamPlayerRequest.Register("name-b", 2, "2020033320"));
//        TeamRequest.Register request = new TeamRequest.Register(leagueTeamName, "invalid-logo-url",
//                playerRegisterRequests, "color code");
//
//        // when & then
//        assertThatThrownBy(() -> leagueTeamService.register(leagueId, manager, request))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessage("잘못된 이미지 url 입니다.");
//    }
//
//    @Nested
//    @DisplayName("팀을 수정할 때")
//    class TeamUpdateTest {
//        private Long teamId = 3L;
//        private Member manager;
//
//        @BeforeEach
//        void setUp() {
//            teamId = 3L;
//            manager = entityUtils.getEntity(1L, Member.class);
//            doNothing().when(s3Service).doesFileExist(anyString());
//        }
//
//        @Test
//        void 팀에_속하지_않은_팀_선수를_삭제하려고_할_때_예외가_발생한다() {
//            // given
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000000"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000000"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of();
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(5L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when & then
//            assertThatThrownBy(() -> leagueTeamService.update(leagueId, request, manager, teamId))
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessageContaining("해당 리그팀에 속하지 않은 선수입니다.");
//        }
//
//        @Test
//        void 정상적으로_이미지_url이_수정된다() {
//            // given
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000000"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000000"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of();
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(3L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when
//            leagueTeamService.update(leagueId, request, manager, teamId);
//
//            // then
//            LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);
//            assertThat(leagueTeam.getName()).isEqualTo(request.name());
//            assertThat(leagueTeam.getLogoImageUrl())
//                    .isEqualTo(request.logoImageUrl().replace(originPrefix, replacePrefix));
//        }
//
//        @Test
//        void 정상적으로_팀선수_정보가_수정된다() {
//            // given
//            Long updatedLeagueTeamPlayerId = 1L;
//            String updatedName = "여름수박진승희";
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000001"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000002"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of(
//                    new LeagueTeamPlayerRequest.Update(updatedLeagueTeamPlayerId, updatedName, 0, "202000003")
//            );
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(3L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when
//            leagueTeamService.update(leagueId, request, manager, teamId);
//
//            // then
//            LeagueTeamPlayer leagueTeamPlayer = entityUtils.getEntity(updatedLeagueTeamPlayerId,
//                    LeagueTeamPlayer.class);
//            assertThat(leagueTeamPlayer.getName()).isEqualTo(updatedName);
//        }
//
//    }
//
//    @Test
//    void 팀을_삭제한다() {
//        // given
//        Long leagueId = 1L;
//        Long leagueTeamId = 3L;
//        List<Long> leagueTeamPlayerIds = List.of(1L, 2L, 3L, 4L);
//
//        // when
//        leagueTeamService.delete(leagueId, manager, leagueTeamId);
//
//        // then
//        assertThat(leagueTeamRepository.findById(leagueTeamId).isEmpty());
//        leagueTeamPlayerIds.stream()
//                .forEach(id -> assertThat(leagueTeamPlayerFixtureRepository.findById(id)).isEmpty());
//    }
//
//}

// 리그팀 서비스
//package com.sports.server.command.leagueteam.application;
//
//import com.sports.server.command.league.domain.League;
//import com.sports.server.command.league.domain.LeagueTeamRepository;
//import com.sports.server.command.team.application.TeamService;
//import com.sports.server.command.team.dto.TeamRequest;
//import com.sports.server.command.member.domain.Member;
//import com.sports.server.common.application.EntityUtils;
//import com.sports.server.common.application.S3Service;
//import com.sports.server.common.exception.UnauthorizedException;
//import com.sports.server.support.ServiceTest;
//import com.sports.server.support.fixture.LeagueTeamPlayerFixtureRepository;
//import java.util.List;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.doNothing;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.jdbc.Sql;
//
//@Sql("/league-fixture.sql")
//@ExtendWith(MockitoExtension.class)
//public class LeagueTeamServiceTest extends ServiceTest {
//
//    @Value("${image.origin-prefix}")
//    private String originPrefix;
//
//    @Value("${image.replaced-prefix}")
//    private String replacePrefix;
//
//    @MockBean
//    private S3Service s3Service;
//
//    @Autowired
//    private EntityUtils entityUtils;
//
//    @Autowired
//    private TeamService leagueTeamService;
//
//    @Autowired
//    private LeagueTeamRepository leagueTeamRepository;
//
//    @Autowired
//    private LeagueTeamPlayerFixtureRepository leagueTeamPlayerFixtureRepository;
//
//    private String imageUrl;
//
//    private Member manager;
//
//    @BeforeEach
//    void setUp() {
//        manager = entityUtils.getEntity(1L, Member.class);
//        imageUrl = originPrefix + "image_url.png";
//    }
//
//    @Test
//    void 리그의_매니저가_아닌_회원이_리그팀을_등록하려고_하면_예외가_발생한다() {
//        // given
//        Long leagueId = 1L;
//        Member nonManager = entityUtils.getEntity(2L, Member.class);
//        TeamRequest.Register request = new TeamRequest.Register("name", imageUrl, List.of(),
//                "color code");
//
//        // when & then
//        assertThrows(UnauthorizedException.class, () -> {
//            leagueTeamService.register(leagueId, nonManager, request);
//        });
//    }
//
//    @Test
//    void 정상적으로_리그팀이_등록된다() {
//        // given
//        Long leagueId = 1L;
//        League league = entityUtils.getEntity(1L, League.class);
//        Member manager = entityUtils.getEntity(1L, Member.class);
//        String leagueTeamName = "name";
//        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                new LeagueTeamPlayerRequest.Register("name-a", 1, "202000000"),
//                new LeagueTeamPlayerRequest.Register("name-b", 2, "202000000"));
//        TeamRequest.Register request = new TeamRequest.Register(leagueTeamName, imageUrl,
//                playerRegisterRequests, "color code");
//        doNothing().when(s3Service).doesFileExist(anyString());
//
//        // when
//        leagueTeamService.register(leagueId, manager, request);
//
//        // then
//        Optional<LeagueTeam> savedLeagueTeamOptional = leagueTeamRepository.findByLeagueAndName(league,
//                leagueTeamName);
//        assertTrue(savedLeagueTeamOptional.isPresent(), "리그팀이 저장되지 않았습니다.");
//
//        LeagueTeam savedLeagueTeam = savedLeagueTeamOptional.get();
//        assertEquals(leagueTeamName, savedLeagueTeam.getName());
//    }
//
//    @Test
//    void origin_prefix가_포함되지_않은_이미지_url을_등록할_경우_예외가_발생한다() {
//        // given
//        Long leagueId = 1L;
//        Member manager = entityUtils.getEntity(1L, Member.class);
//        String leagueTeamName = "name";
//        List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                new LeagueTeamPlayerRequest.Register("name-a", 1, "2020033320"),
//                new LeagueTeamPlayerRequest.Register("name-b", 2, "2020033320"));
//        TeamRequest.Register request = new TeamRequest.Register(leagueTeamName, "invalid-logo-url",
//                playerRegisterRequests, "color code");
//
//        // when & then
//        assertThatThrownBy(() -> leagueTeamService.register(leagueId, manager, request))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessage("잘못된 이미지 url 입니다.");
//    }
//
//    @Nested
//    @DisplayName("리그팀을 수정할 때")
//    class LeagueTeamUpdateTest {
//
//        private Long leagueId;
//        private Long teamId = 3L;
//        private Member manager;
//
//        @BeforeEach
//        void setUp() {
//            leagueId = 1L;
//            teamId = 3L;
//            manager = entityUtils.getEntity(1L, Member.class);
//            doNothing().when(s3Service).doesFileExist(anyString());
//        }
//
//        @Test
//        void 리그팀에_속하지_않은_리그팀_선수를_삭제하려고_할_때_예외가_발생한다() {
//            // given
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000000"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000000"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of();
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(5L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when & then
//            assertThatThrownBy(() -> leagueTeamService.update(leagueId, request, manager, teamId))
//                    .isInstanceOf(IllegalStateException.class)
//                    .hasMessageContaining("해당 리그팀에 속하지 않은 선수입니다.");
//        }
//
//        @Test
//        void 정상적으로_이미지_url이_수정된다() {
//            // given
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000000"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000000"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of();
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(3L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when
//            leagueTeamService.update(leagueId, request, manager, teamId);
//
//            // then
//            LeagueTeam leagueTeam = entityUtils.getEntity(teamId, LeagueTeam.class);
//            assertThat(leagueTeam.getName()).isEqualTo(request.name());
//            assertThat(leagueTeam.getLogoImageUrl())
//                    .isEqualTo(request.logoImageUrl().replace(originPrefix, replacePrefix));
//        }
//
//        @Test
//        void 정상적으로_리그팀_선수_정보가_수정된다() {
//            // given
//            Long updatedLeagueTeamPlayerId = 1L;
//            String updatedName = "여름수박진승희";
//            List<LeagueTeamPlayerRequest.Register> playerRegisterRequests = List.of(
//                    new LeagueTeamPlayerRequest.Register("name-a", 1, "202000001"),
//                    new LeagueTeamPlayerRequest.Register("name-b", 2, "202000002"));
//            List<LeagueTeamPlayerRequest.Update> playerUpdateRequests = List.of(
//                    new LeagueTeamPlayerRequest.Update(updatedLeagueTeamPlayerId, updatedName, 0, "202000003")
//            );
//            TeamRequest.Update request = new TeamRequest.Update(
//                    "name", imageUrl, playerRegisterRequests, playerUpdateRequests, List.of(3L));
//            doNothing().when(s3Service).doesFileExist(anyString());
//
//            // when
//            leagueTeamService.update(leagueId, request, manager, teamId);
//
//            // then
//            LeagueTeamPlayer leagueTeamPlayer = entityUtils.getEntity(updatedLeagueTeamPlayerId,
//                    LeagueTeamPlayer.class);
//            assertThat(leagueTeamPlayer.getName()).isEqualTo(updatedName);
//        }
//
//    }
//
//    @Test
//    void 리그팀을_삭제한다() {
//        // given
//        Long leagueId = 1L;
//        Long leagueTeamId = 3L;
//        List<Long> leagueTeamPlayerIds = List.of(1L, 2L, 3L, 4L);
//
//        // when
//        leagueTeamService.delete(leagueId, manager, leagueTeamId);
//
//        // then
//        assertThat(leagueTeamRepository.findById(leagueTeamId).isEmpty());
//        leagueTeamPlayerIds.stream()
//                .forEach(id -> assertThat(leagueTeamPlayerFixtureRepository.findById(id)).isEmpty());
//    }
//}
