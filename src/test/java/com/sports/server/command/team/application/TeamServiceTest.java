package com.sports.server.command.team.application;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.member.domain.MemberRepository;
import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.*;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


@Sql("/team-fixture.sql")
public class TeamServiceTest extends ServiceTest {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    @MockitoBean
    private S3Service s3Service;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String imageUrl;
    private Member manager;

    @BeforeEach
    void setUp() throws IOException {
        imageUrl = originPrefix + "image_url.png";
        manager = memberRepository.findMemberByEmailWithOrganization("john@example.com")
                .orElseThrow();

        BufferedImage stub = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(stub, "png", png);
        when(s3Service.download(anyString())).thenReturn(png.toByteArray());
        doNothing().when(s3Service).upload(anyString(), any(byte[].class), anyString());
    }

    @Nested
    @DisplayName("팀을 생성할 때")
    class RegisterTeam{
        @Test
        void origin_prefix가_포함되지_않은_이미지_url을_등록할_경우_예외가_발생한다() {
            // given
            List<TeamRequest.TeamPlayerRegister> playerRegisterRequests = List.of(
                    new TeamRequest.TeamPlayerRegister(1L, 10),
                    new TeamRequest.TeamPlayerRegister(2L, 7));

            TeamRequest.Register request = new TeamRequest.Register("name", "invalid-logo-url",
                    "사회과학대학","color code", playerRegisterRequests, null);

            // when & then
            assertThatThrownBy(() -> teamService.register(manager, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("잘못된 이미지 url 입니다.");
        }

        @Test
        void 존재하지_않는_단위를_요청할_경우_예외가_발생한다() {
            // given
            List<TeamRequest.TeamPlayerRegister> playerRegisterRequests = List.of(
                    new TeamRequest.TeamPlayerRegister(1L, 10),
                    new TeamRequest.TeamPlayerRegister(2L, 7));

            TeamRequest.Register request = new TeamRequest.Register("name", imageUrl,
                    "invalid unit","color code", playerRegisterRequests, null);

            // when & then
            assertThatThrownBy(() -> teamService.register(manager, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("팀을 수정할 때")
    class TeamUpdateTest{

        @Test
        void 일부_필드가_없어도_정상적으로_수정된다(){
            // given
            Long teamId = 1L;
            String newName = "경영 경주마";
            TeamRequest.Update request = new TeamRequest.Update(newName, imageUrl, null, null, null);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when
            teamService.update(manager, request, teamId);
            Team team = entityUtils.getEntity(1L, Team.class);

            // then
            assertThat(team.getName()).isEqualTo(newName);
        }

        @Test
        void origin_prefix가_포함되지_않은_이미지_url로_수정할_경우_예외가_발생한다(){
            // given
            Long teamId = 1L;
            String invalidUrl = "invalid url";
            TeamRequest.Update request = new TeamRequest.Update(null, invalidUrl, null, null, null);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when & then
            assertThatThrownBy(() -> teamService.update(manager, request, teamId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("잘못된 이미지 url 입니다.");
        }

        @Test
        void origin_prefix가_앞이_아니라_중간에_들어있는_이미지_url로_수정할_경우_예외가_발생하고_로고는_그대로다(){
            // given
            String url = "https://evil.example/x#" + imageUrl;
            TeamRequest.Update request = new TeamRequest.Update(null, url, null, null, null);

            // when & then
            assertThatThrownBy(() -> teamService.update(manager, request, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("잘못된 이미지 url 입니다.");
            assertThat(entityUtils.getEntity(1L, Team.class).getLogoImageUrl())
                    .isEqualTo("https://example.com/logos/wildhorse.png");
        }

        @Test
        void 다른_팀이_쓰는_로고_주소로_바꾼_뒤_자기_로고를_지워도_그_객체는_지워지지_않는다(){
            // given: 팀 2가 쓰는 로고를 팀 1이 자기 로고로 저장한다
            jdbcTemplate.update("UPDATE teams SET logo_image_url = ? WHERE id = 2", replacePrefix + "shared.png");
            teamService.update(manager, new TeamRequest.Update(null, originPrefix + "shared.png", null, null, null), 1L);
            String sharedUrl = entityUtils.getEntity(1L, Team.class).getLogoImageUrl();
            assertThat(sharedUrl).isEqualTo(entityUtils.getEntity(2L, Team.class).getLogoImageUrl());
            org.mockito.Mockito.clearInvocations(s3Service);

            // when
            teamService.deleteLogoImage(manager, 1L);

            // then
            verify(s3Service, never()).deleteFile(anyString());
            assertThat(entityUtils.getEntity(2L, Team.class).getLogoImageUrl()).isEqualTo(sharedUrl);
        }

        @Test
        void 아무도_쓰지_않는_자기_로고를_지우면_객체가_지워진다(){
            // given
            jdbcTemplate.update("UPDATE teams SET logo_image_url = ? WHERE id = 1", replacePrefix + "mine.png");

            // when
            teamService.deleteLogoImage(manager, 1L);

            // then
            verify(s3Service).deleteFile("mine.png");
        }

        @Test
        void 존재하지_않는_팀의_수정을_시도할_경우_예외가_발생한다(){
            // given
            Long teamId = 999L;
            TeamRequest.Update request = new TeamRequest.Update("newName", null, null, null, null);

            // when & then
            assertThatThrownBy(() -> teamService.update(manager, request, teamId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 존재하지_않는_단위를_요청할_경우_예외가_발생한다(){
            // given
            Long teamId =  1L;
            TeamRequest.Update request = new TeamRequest.Update(null, null, "invalid unit", null, null);

            // when & then
            assertThatThrownBy(() -> teamService.update(manager, request, teamId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION);
        }

        @Test
        void teamPlayers가_null이면_선수를_수정하지_않는다() {
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            TeamRequest.Update request = new TeamRequest.Update(null, null, null, null, null);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when
            teamService.update(manager, request, teamId);
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId);

            // then
            assertThat(teamPlayers).hasSize(2);
        }

        @Test
        void 신규_선수가_포함되면_팀에_추가된다() {
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            List<TeamRequest.TeamPlayerRegister> players = List.of(
                    new TeamRequest.TeamPlayerRegister(1L, 10),
                    new TeamRequest.TeamPlayerRegister(2L, 7)
            );
            TeamRequest.Update request = new TeamRequest.Update(null, null, null, null, players);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when
            teamService.update(manager, request, teamId);
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId);

            // then
            assertThat(teamPlayers).hasSize(4);
        }

        @Test
        void 기존_선수가_포함되면_jerseyNumber가_수정된다() {
            // given
            Long teamId = 1L; // 기존 선수: 3L(등번호 9), 4L(등번호 11)
            List<TeamRequest.TeamPlayerRegister> players = List.of(
                    new TeamRequest.TeamPlayerRegister(3L, 99)
            );
            TeamRequest.Update request = new TeamRequest.Update(null, null, null, null, players);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when
            teamService.update(manager, request, teamId);
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId);

            // then
            assertThat(teamPlayers).hasSize(2);
            Optional<TeamPlayer> updatedPlayer = teamPlayers.stream()
                    .filter(tp -> tp.getPlayer().getId().equals(3L))
                    .findFirst();
            assertThat(updatedPlayer).isPresent();
            assertThat(updatedPlayer.get().getJerseyNumber()).isEqualTo(99);
        }

        @Test
        void 존재하지_않는_선수가_포함되면_예외가_발생한다() {
            // given
            Long teamId = 1L;
            List<TeamRequest.TeamPlayerRegister> players = List.of(
                    new TeamRequest.TeamPlayerRegister(999L, 10)
            );
            TeamRequest.Update request = new TeamRequest.Update(null, null, null, null, players);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when & then
            assertThatThrownBy(() -> teamService.update(manager, request, teamId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(PlayerErrorMessages.PLAYER_NOT_EXIST_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("팀에 선수를 추가할 때")
    class AddPlayersToTeam{

        @Test
        void 정상적으로_추가된다(){
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            List<TeamRequest.TeamPlayerRegister> playerToAdd = List.of(
                    new TeamRequest.TeamPlayerRegister(1L, 10),
                    new TeamRequest.TeamPlayerRegister(2L, 7)
            );

            // when
            teamService.addPlayersToTeam(manager, teamId, playerToAdd);
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId);

            //then
            assertThat(teamPlayers.size()).isEqualTo(4);
        }

        @Test
        void 이미_팀에_소속된_선수_추가를_시도할_경우_예외가_발생한다(){
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            List<TeamRequest.TeamPlayerRegister> playerToAdd = List.of(
                    new TeamRequest.TeamPlayerRegister(3L, 10),
                    new TeamRequest.TeamPlayerRegister(2L, 7)
            );

            // when & then
            assertThatThrownBy(() -> teamService.addPlayersToTeam(manager, teamId, playerToAdd))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 팀에 소속된 선수입니다.");
        }

        @Test
        void 빈_리스트일_경우_아무_선수도_추가하지_않는다(){
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            List<TeamRequest.TeamPlayerRegister> playerToAdd = List.of();

            // when
            teamService.addPlayersToTeam(manager, teamId, playerToAdd);
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findTeamPlayersWithPlayerByTeamId(teamId);

            //then
            assertThat(teamPlayers.size()).isEqualTo(2);
        }

        @Test
        void 존재하지_않는_선수_추가를_시도할_경우_예외가_발생한다(){
            // given
            Long teamId = 1L;
            List<TeamRequest.TeamPlayerRegister> playerToAdd = List.of(
                    new TeamRequest.TeamPlayerRegister(999L, 10)
            );

            // when & then
            assertThatThrownBy(() -> teamService.addPlayersToTeam(manager, teamId, playerToAdd))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(PlayerErrorMessages.PLAYER_NOT_EXIST_EXCEPTION);
        }
    }

    @Nested
    @DisplayName("팀에서 선수를 삭제할 때")
    class RemovePlayersToTeam {

        @Test
        void 정상적으로_삭제된다() {
            // given
            Long teamPlayerIdToDelete = 1L;

            // when
            teamService.deleteTeamPlayer(manager, teamPlayerIdToDelete);

            //then
            Optional<TeamPlayer> result = teamPlayerRepository.findById(teamPlayerIdToDelete);
            assertThat(result).isEmpty();
        }

        @Test
        void 존재하지_않는_팀선수_삭제를_시도할_경우_예외가_발생한다(){
            // given
            Long nonExistentTeamPlayerId = 999L;

            // when & then
            assertThatThrownBy(() -> teamService.deleteTeamPlayer(manager, nonExistentTeamPlayerId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Test
    void 팀_이미지가_정상적으로_삭제된다(){
        // given
        Long teamId = 1L;

        // when
        teamService.deleteLogoImage(manager, teamId);

        // then
        Team team = entityUtils.getEntity(teamId, Team.class);
        assertThat(team.getLogoImageUrl()).isEqualTo("");
    }

    @Test
    void 팀_이미지를_삭제하면_S3_의_정규화본도_지운다(){
        // given: 로고를 바꾸면 정규화본(normalized/...)으로 주소가 바뀐다
        Long teamId = 1L;
        teamService.update(manager, new TeamRequest.Update(null, imageUrl, null, null, null), teamId);

        // when
        teamService.deleteLogoImage(manager, teamId);

        // then
        verify(s3Service).deleteFile("normalized/image_url.webp");
    }

    @Nested
    @DisplayName("로고를 바꾸면")
    class LogoNormalization {

        private static final Long TEAM_ID = 1L;

        @Test
        void 정규화본을_새_키에_WebP_로_올리고_팀_로고를_그_주소로_바꾼_뒤_원본을_치운다() {
            // when
            teamService.update(manager, new TeamRequest.Update(null, imageUrl, null, null, null), TEAM_ID);

            // then
            verify(s3Service).upload(eq("normalized/image_url.webp"), any(byte[].class), eq("image/webp"));
            verify(s3Service, never()).upload(eq("image_url.png"), any(byte[].class), anyString());
            assertThat(logoOf(TEAM_ID)).isEqualTo(replacePrefix + "normalized/image_url.webp");
            verify(s3Service).deleteFile("image_url.png");
        }

        @Test
        void 원본을_읽지_못하면_원본_주소를_그대로_쓴다() {
            // given
            when(s3Service.download(anyString())).thenReturn("not-an-image".getBytes());

            // when
            teamService.update(manager, new TeamRequest.Update(null, imageUrl, null, null, null), TEAM_ID);

            // then
            assertThat(logoOf(TEAM_ID)).isEqualTo(replacePrefix + "image_url.png");
            verify(s3Service, never()).upload(anyString(), any(byte[].class), anyString());
            verify(s3Service, never()).deleteFile(anyString());
        }

        @Test
        void 정규화본_업로드에_실패하면_원본_주소를_그대로_쓰고_원본도_지우지_않는다() {
            // given
            doThrow(new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일 업로드에 실패했습니다."))
                    .when(s3Service).upload(anyString(), any(byte[].class), anyString());

            // when
            teamService.update(manager, new TeamRequest.Update(null, imageUrl, null, null, null), TEAM_ID);

            // then
            assertThat(logoOf(TEAM_ID)).isEqualTo(replacePrefix + "image_url.png");
            verify(s3Service, never()).deleteFile(anyString());
        }

        @Test
        void 업로드_직후의_원본_주소를_다시_보내도_정규화본을_유지한다() {
            // given
            teamService.update(manager, new TeamRequest.Update(null, imageUrl, null, null, null), TEAM_ID);

            // when: 화면이 들고 있던 원본 주소로 이름만 바꾼다
            teamService.update(manager, new TeamRequest.Update("경영 경주마", imageUrl, null, null, null), TEAM_ID);

            // then
            assertThat(logoOf(TEAM_ID)).isEqualTo(replacePrefix + "normalized/image_url.webp");
        }

        @Test
        void 같은_원본을_쓰는_지운_팀의_주소도_바꾼다() {
            // given
            String original = replacePrefix + "image_url.png";
            jdbcTemplate.update("UPDATE teams SET logo_image_url = ? WHERE id IN (2, 5)", original);
            jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 5");

            // when
            int updated = teamRepository.replaceLogoImageUrl(original, replacePrefix + "normalized/image_url.webp");

            // then
            assertThat(updated).isEqualTo(2);
            assertThat(jdbcTemplate.queryForObject("SELECT logo_image_url FROM teams WHERE id = 5", String.class))
                    .isEqualTo(replacePrefix + "normalized/image_url.webp");
        }

        private String logoOf(Long teamId) {
            return jdbcTemplate.queryForObject("SELECT logo_image_url FROM teams WHERE id = ?", String.class, teamId);
        }
    }
}
