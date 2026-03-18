package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.dto.*;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;
import com.sports.server.command.nl.exception.NlErrorMessages;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NlServiceTest {

    @InjectMocks
    private NlService nlService;

    @Mock
    private NlClient nlClient;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamPlayerRepository teamPlayerRepository;

    @Mock
    private LeagueTeamRepository leagueTeamRepository;

    @Mock
    private EntityUtils entityUtils;

    @Mock
    private PlayerService playerService;

    @Mock
    private TeamService teamService;

    private Team mockTeam;
    private League mockLeague;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        mockTeam = mock(Team.class);
        mockLeague = mock(League.class);
        mockMember = mock(Member.class);
        lenient().when(mockTeam.getName()).thenReturn("정치외교학과 DPS");
        lenient().when(mockLeague.isManagedBy(mockMember)).thenReturn(true);
        lenient().when(entityUtils.getEntity(186L, League.class)).thenReturn(mockLeague);
        lenient().when(leagueTeamRepository.findByLeagueAndTeam(mockLeague, mockTeam))
                .thenReturn(Optional.of(mock(LeagueTeam.class)));
    }

    @Nested
    @DisplayName("process")
    class Process {

        @Test
        @DisplayName("정상적인 텍스트를 파싱하여 프리뷰를 반환한다")
        void 정상_파싱_프리뷰_반환() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    List.of(), "홍길동 202600001 10\n김철수 202600002 7"
            );

            given(nlClient.parsePlayers(anyString(), anyList()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600002", 7)
                    )));

            // when
            NlProcessResponse response = nlService.process(request);

            // then
            assertThat(response.preview()).isNotNull();
            assertThat(response.preview().players()).hasSize(2);
            assertThat(response.preview().total()).isEqualTo(2);
            assertThat(response.displayMessage()).contains("2명");
        }

        @Test
        @DisplayName("입력 내 학번이 중복되면 첫 번째만 남기고 제거한다")
        void 입력_내_학번_중복_제거() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    List.of(), "홍길동 202600001 10\n김철수 202600001 7"
            );

            given(nlClient.parsePlayers(anyString(), anyList()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600001", 7)
                    )));

            // when
            NlProcessResponse response = nlService.process(request);

            // then
            assertThat(response.preview().players()).hasSize(1);
            assertThat(response.preview().players().get(0).name()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("학번이 원본 텍스트에 없으면 파싱 실패로 처리한다")
        void 학번_원본_대조_실패() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    List.of(), "홍길동 20260001 10"  // 8자리 — 원본에 9자리 없음
            );

            given(nlClient.parsePlayers(anyString(), anyList()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10)  // LLM이 9자리로 보정
                    )));

            // when
            NlProcessResponse response = nlService.process(request);

            // then
            assertThat(response.preview().players()).isEmpty();
            assertThat(response.preview().parseFailedLines()).hasSize(1);
            assertThat(response.preview().parseFailedLines().get(0).reason())
                    .contains(NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL);
        }

        @Test
        @DisplayName("파싱에 실패하면 텍스트 메시지를 반환한다")
        void 파싱_실패시_텍스트_메시지() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    List.of(), "안녕하세요"
            );

            given(nlClient.parsePlayers(anyString(), anyList()))
                    .willReturn(NlParseResult.ofText("선수 정보를 입력해주세요."));

            // when
            NlProcessResponse response = nlService.process(request);

            // then
            assertThat(response.preview()).isNull();
            assertThat(response.displayMessage()).isEqualTo("선수 정보를 입력해주세요.");
        }

        @Test
        @DisplayName("선수 정보가 없으면 안내 메시지를 반환한다")
        void 선수_정보_없음() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    List.of(), "아무 내용 202600001"
            );

            given(nlClient.parsePlayers(anyString(), anyList()))
                    .willReturn(NlParseResult.ofPlayers(List.of()));

            // when
            NlProcessResponse response = nlService.process(request);

            // then
            assertThat(response.preview()).isNull();
            assertThat(response.displayMessage()).isEqualTo(NlErrorMessages.NO_PLAYER_INFO);
        }
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("신규 선수를 생성하고 팀에 배정한다")
        void 신규_선수_생성_및_팀_배정() {
            // given
            NlExecuteRequest request = new NlExecuteRequest(186L, 1L, List.of(
                    new NlExecuteRequest.PlayerData("홍길동", "202600001", 10)
            ));

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(playerService.register(any())).willReturn(100L);

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(1);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(playerService).register(any());
            verify(teamService).addPlayersToTeam(eq(1L), anyList());
        }

        @Test
        @DisplayName("기존 선수는 생성하지 않고 팀에만 배정한다")
        void 기존_선수_팀_배정만() {
            // given
            NlExecuteRequest request = new NlExecuteRequest(186L, 1L, List.of(
                    new NlExecuteRequest.PlayerData("김철수", "202600002", 7)
            ));

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getId()).willReturn(42L);
            given(existingPlayer.getStudentNumber()).willReturn("202600002");

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(0);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(playerService, never()).register(any());
            verify(teamService).addPlayersToTeam(eq(1L), anyList());
        }

        @Test
        @DisplayName("이미 팀에 소속된 선수는 스킵한다")
        void 이미_소속된_선수_스킵() {
            // given
            NlExecuteRequest request = new NlExecuteRequest(186L, 1L, List.of(
                    new NlExecuteRequest.PlayerData("이영희", "202600003", 5)
            ));

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getId()).willReturn(42L);
            given(existingPlayer.getStudentNumber()).willReturn("202600003");

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of(42L));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(0);
            assertThat(response.result().assigned()).isEqualTo(0);
            assertThat(response.result().skipped()).isEqualTo(1);
            verify(teamService, never()).addPlayersToTeam(anyLong(), anyList());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("팀이 리그에 소속되지 않으면 예외가 발생한다")
        void 팀_리그_불일치_예외() {
            // given
            NlExecuteRequest request = new NlExecuteRequest(
                    186L, 1L, List.of(
                    new NlExecuteRequest.PlayerData("홍길동", "202600001", 10)
            ));

            Team otherTeam = mock(Team.class);
            given(entityUtils.getEntity(1L, Team.class)).willReturn(otherTeam);
            given(leagueTeamRepository.findByLeagueAndTeam(mockLeague, otherTeam))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> nlService.execute(request, mockMember))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(NlErrorMessages.TEAM_NOT_IN_LEAGUE);
        }

        @Test
        @DisplayName("execute에서 중복 학번 입력은 첫 번째만 처리하고 나머지는 스킵한다")
        void execute_중복_학번_스킵() {
            // given
            NlExecuteRequest request = new NlExecuteRequest(186L, 1L, List.of(
                    new NlExecuteRequest.PlayerData("홍길동", "202600001", 10),
                    new NlExecuteRequest.PlayerData("김철수", "202600001", 7)
            ));

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(playerService.register(any())).willReturn(100L);

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(1);
            assertThat(response.result().assigned()).isEqualTo(1);
            assertThat(response.result().skipped()).isEqualTo(1);
            verify(playerService, times(1)).register(any());
        }
    }
}
