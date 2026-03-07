package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.dto.NlExecuteRequest;
import com.sports.server.command.nl.dto.NlExecuteResponse;
import com.sports.server.command.nl.dto.NlProcessRequest;
import com.sports.server.command.nl.dto.NlProcessResponse;
import com.sports.server.command.nl.infra.GeminiFunctionCallResponse;
import com.sports.server.command.nl.infra.GeminiFunctionCallResponse.*;
import com.sports.server.command.nl.infra.NlGeminiClient;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.command.nl.exception.NlErrorMessages;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class NlServiceTest {

    @InjectMocks
    private NlService nlService;

    @Mock
    private NlGeminiClient nlGeminiClient;

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
                    186L, 1L, List.of(),
                    "홍길동 202600001 10\n김철수 202600002 7"
            );

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildFunctionCallResponse(List.of(
                            Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10),
                            Map.of("name", "김철수", "studentNumber", "202600002", "jerseyNumber", 7)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview()).isNotNull();
            assertThat(response.preview().players()).hasSize(2);
            assertThat(response.preview().players().get(0).status()).isEqualTo("NEW");
            assertThat(response.preview().players().get(1).status()).isEqualTo("NEW");
            assertThat(response.preview().summary().newPlayers()).isEqualTo(2);
        }

        @Test
        @DisplayName("이미 DB에 존재하는 선수는 EXISTS로 분류한다")
        void 기존_선수_EXISTS_분류() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(),
                    "홍길동 202600001 10"
            );

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getStudentNumber()).willReturn("202600001");
            given(existingPlayer.getId()).willReturn(42L);

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildFunctionCallResponse(List.of(
                            Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players().get(0).status()).isEqualTo("EXISTS");
            assertThat(response.preview().players().get(0).existingPlayerId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("이미 팀에 소속된 선수는 ALREADY_IN_TEAM으로 분류한다")
        void 팀_소속_선수_ALREADY_IN_TEAM_분류() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(),
                    "홍길동 202600001 10"
            );

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getStudentNumber()).willReturn("202600001");
            given(existingPlayer.getId()).willReturn(42L);

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildFunctionCallResponse(List.of(
                            Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of(42L));

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players().get(0).status()).isEqualTo("ALREADY_IN_TEAM");
        }

        @Test
        @DisplayName("입력 내 학번이 중복되면 DUPLICATE_IN_INPUT으로 분류한다")
        void 입력_내_학번_중복() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(),
                    "홍길동 202600001 10\n김철수 202600001 7"
            );

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildFunctionCallResponse(List.of(
                            Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10),
                            Map.of("name", "김철수", "studentNumber", "202600001", "jerseyNumber", 7)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players().get(0).status()).isEqualTo("NEW");
            assertThat(response.preview().players().get(1).status()).isEqualTo("DUPLICATE_IN_INPUT");
        }

        @Test
        @DisplayName("학번이 원본 텍스트에 없으면 파싱 실패로 처리한다")
        void 학번_원본_대조_실패() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(),
                    "홍길동 20260001 10"  // 8자리 — 원본에 9자리 없음
            );

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildFunctionCallResponse(List.of(
                            Map.of("name", "홍길동", "studentNumber", "202600001", "jerseyNumber", 10)  // LLM이 9자리로 보정
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players()).isEmpty();
            assertThat(response.preview().parseFailedLines()).hasSize(1);
            assertThat(response.preview().parseFailedLines().get(0).reason())
                    .contains(NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL);
        }

        @Test
        @DisplayName("Gemini가 Function Call 대신 텍스트를 반환하면 메시지를 반환한다")
        void Gemini_텍스트_반환시_메시지() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(), "안녕하세요"
            );

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlGeminiClient.parsePlayers(anyString(), anyList()))
                    .willReturn(buildTextResponse("선수 정보를 입력해주세요."));

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview()).isNull();
            assertThat(response.displayMessage()).isEqualTo("선수 정보를 입력해주세요.");
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
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(), "홍길동 202600001 10"
            );

            Team otherTeam = mock(Team.class);
            given(entityUtils.getEntity(1L, Team.class)).willReturn(otherTeam);
            given(leagueTeamRepository.findByLeagueAndTeam(mockLeague, otherTeam))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> nlService.process(request, mockMember))
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

    // Helper: Function Call 응답 빌드
    private GeminiFunctionCallResponse buildFunctionCallResponse(List<Map<String, Object>> players) {
        Map<String, Object> args = new HashMap<>();
        args.put("players", players);
        FunctionCall fc = new FunctionCall("parse_players", args);
        Part part = new Part(null, fc);
        Content content = new Content(List.of(part));
        Candidate candidate = new Candidate(content);
        return new GeminiFunctionCallResponse(List.of(candidate));
    }

    // Helper: 텍스트 응답 빌드
    private GeminiFunctionCallResponse buildTextResponse(String text) {
        Part part = new Part(text, null);
        Content content = new Content(List.of(part));
        Candidate candidate = new Candidate(content);
        return new GeminiFunctionCallResponse(List.of(candidate));
    }
}
