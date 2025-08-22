package com.sports.server.command.team.application;

import com.sports.server.command.player.exception.PlayerErrorMessages;
import com.sports.server.command.team.domain.*;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;


@Sql("/team-fixture.sql")
public class TeamServiceTest extends ServiceTest {

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private EntityUtils entityUtils;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    private String imageUrl;

    @BeforeEach
    void setUp() {
        imageUrl = originPrefix + "image_url.png";
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
                    "사회과학대학","color code", playerRegisterRequests);

            // when & then
            assertThatThrownBy(() -> teamService.register(request))
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
                    "invalid unit","color code", playerRegisterRequests);

            // when & then
            assertThatThrownBy(() -> teamService.register(request))
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
            TeamRequest.Update request = new TeamRequest.Update(newName, imageUrl, null, null);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when
            teamService.update(request, teamId);
            Team team = entityUtils.getEntity(1L, Team.class);

            // then
            assertThat(team.getName()).isEqualTo(newName);
        }

        @Test
        void origin_prefix가_포함되지_않은_이미지_url로_수정할_경우_예외가_발생한다(){
            // given
            Long teamId = 1L;
            String invalidUrl = "invalid url";
            TeamRequest.Update request = new TeamRequest.Update(null, invalidUrl, null, null);

            doNothing().when(s3Service).doesFileExist(anyString());

            // when & then
            assertThatThrownBy(() -> teamService.update(request, teamId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("잘못된 이미지 url 입니다.");
        }

        @Test
        void 존재하지_않는_팀의_수정을_시도할_경우_예외가_발생한다(){
            // given
            Long teamId = 999L;
            TeamRequest.Update request = new TeamRequest.Update("newName", null, null, null);

            // when & then
            assertThatThrownBy(() -> teamService.update(request, teamId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 존재하지_않는_단위를_요청할_경우_예외가_발생한다(){
            // given
            Long teamId =  1L;
            TeamRequest.Update request = new TeamRequest.Update(null, null, "invalid unit", null);

            // when & then
            assertThatThrownBy(() -> teamService.update(request, teamId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION);
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
            teamService.addPlayersToTeam(teamId, playerToAdd);
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
            assertThatThrownBy(() -> teamService.addPlayersToTeam(teamId, playerToAdd))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 팀에 소속된 선수입니다.");
        }

        @Test
        void 빈_리스트일_경우_아무_선수도_추가하지_않는다(){
            // given
            Long teamId = 1L; // 기존 선수: 3L, 4L 두 명
            List<TeamRequest.TeamPlayerRegister> playerToAdd = List.of();

            // when
            teamService.addPlayersToTeam(teamId, playerToAdd);
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
            assertThatThrownBy(() -> teamService.addPlayersToTeam(teamId, playerToAdd))
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
            teamService.delete(teamPlayerIdToDelete);

            //then
            Optional<TeamPlayer> result = teamPlayerRepository.findById(teamPlayerIdToDelete);
            assertThat(result).isEmpty();
        }

        @Test
        void 존재하지_않는_팀선수_삭제를_시도할_경우_예외가_발생한다(){
            // given
            Long nonExistentTeamPlayerId = 999L;

            // when & then
            assertThatThrownBy(() -> teamService.deleteTeamPlayer(nonExistentTeamPlayerId))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Test
    void 팀_이미지가_정상적으로_삭제된다(){
        // given
        Long teamId = 1L;

        // when
        teamService.deleteLogoImage(teamId);

        // then
        Team team = entityUtils.getEntity(teamId, Team.class);
        assertThat(team.getLogoImageUrl()).isEqualTo("");
    }
}
