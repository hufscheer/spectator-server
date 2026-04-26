package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.nl.domain.PlayerStatus;
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
import static org.assertj.core.api.Assertions.tuple;
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
        Organization mockOrganization = mock(Organization.class);
        lenient().when(mockTeam.getName()).thenReturn("정치외교학과 DPS");
        lenient().when(mockLeague.isManagedBy(mockMember)).thenReturn(true);
        lenient().when(mockMember.getOrganization()).thenReturn(mockOrganization);
        lenient().when(mockOrganization.getStudentNumberDigits()).thenReturn(9);
        lenient().when(entityUtils.getEntity(186L, League.class)).thenReturn(mockLeague);
        lenient().when(leagueTeamRepository.findByLeagueAndTeam(mockLeague, mockTeam))
                .thenReturn(Optional.of(mock(LeagueTeam.class)));
    }

    @Nested
    @DisplayName("process - 팀 컨텍스트 포함")
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
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600002", 7)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview()).isNotNull();
            assertThat(response.preview().players()).hasSize(2);
            assertThat(response.preview().players().get(0).status()).isEqualTo(PlayerStatus.NEW);
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
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players().get(0).status()).isEqualTo(PlayerStatus.EXISTS);
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
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of(42L));

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players().get(0).status()).isEqualTo(PlayerStatus.ALREADY_IN_TEAM);
        }

        @Test
        @DisplayName("입력 내 학번이 중복되면 첫 번째만 남기고 제거한다")
        void 입력_내_학번_중복_제거() {
            // given
            NlProcessRequest request = new NlProcessRequest(
                    186L, 1L, List.of(),
                    "홍길동 202600001 10\n김철수 202600001 7"
            );

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600001", 7)
                    )));
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());

            // when
            NlProcessResponse response = nlService.process(request, mockMember);

            // then
            assertThat(response.preview().players()).hasSize(1);
            assertThat(response.preview().players().get(0).name()).isEqualTo("홍길동");
            assertThat(response.preview().players().get(0).status()).isEqualTo(PlayerStatus.NEW);
        }
    }

    @Nested
    @DisplayName("parse - 팀 컨텍스트 없이 파싱만")
    class Parse {

        @Test
        @DisplayName("정상적인 텍스트를 파싱하여 프리뷰를 반환한다")
        void 정상_파싱_프리뷰_반환() {
            // given
            NlParseRequest request = new NlParseRequest(
                    List.of(), "홍길동 202600001 10\n김철수 202600002 7"
            );

            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600002", 7)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

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
            NlParseRequest request = new NlParseRequest(
                    List.of(), "홍길동 202600001 10\n김철수 202600001 7"
            );

            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10),
                            new ParsedPlayer("김철수", "202600001", 7)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview().players()).hasSize(1);
            assertThat(response.preview().players().get(0).name()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("학번이 원본 텍스트에 없으면 파싱 실패로 처리한다")
        void 학번_원본_대조_실패() {
            // given
            NlParseRequest request = new NlParseRequest(
                    List.of(), "홍길동 20260001 10"
            );

            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("홍길동", "202600001", 10)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview().players()).isEmpty();
            assertThat(response.preview().parseFailedLines()).hasSize(1);
        }

        @Test
        @DisplayName("파싱에 실패하면 텍스트 메시지를 반환한다")
        void 파싱_실패시_텍스트_메시지() {
            // given
            NlParseRequest request = new NlParseRequest(
                    List.of(), "안녕하세요"
            );

            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofText("선수 정보를 입력해주세요."));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview()).isNull();
            assertThat(response.displayMessage()).isEqualTo("선수 정보를 입력해주세요.");
        }

        @Test
        @DisplayName("계정 자릿수와 다른 학번은 Gemini 결과와 무관하게 failedLines에 포함된다")
        void 자릿수_불일치_학번_failedLines_포함() {
            // given: 10자리 계정인데 원문에 9자리 학번이 섞여 있고 Gemini가 이를 누락
            given(mockMember.getOrganization().getStudentNumberDigits()).willReturn(10);
            NlParseRequest request = new NlParseRequest(
                    List.of(), "경희일 1234543221 12\n경희이 123456789 2"
            );
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("경희일", "1234543221", 12)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview().players()).hasSize(1);
            assertThat(response.preview().parseFailedLines())
                    .extracting(NlFailedLine::studentNumber, NlFailedLine::index)
                    .contains(tuple("123456789", 2));
        }

        @Test
        @DisplayName("Gemini가 파싱한 9자리 학번은 이름/등번호를 포함해 failedLines에 담긴다")
        void 자릿수_불일치_Gemini_파싱값_이름_등번호_보존() {
            // given: 10자리 계정인데 Gemini가 9자리 학번을 이름/등번호와 함께 파싱
            given(mockMember.getOrganization().getStudentNumberDigits()).willReturn(10);
            NlParseRequest request = new NlParseRequest(
                    List.of(), "경희이 123456789 2"
            );
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("경희이", "123456789", 2)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview().players()).isEmpty();
            assertThat(response.preview().parseFailedLines())
                    .extracting(NlFailedLine::studentNumber, NlFailedLine::name, NlFailedLine::jerseyNumber)
                    .contains(tuple("123456789", "경희이", 2));
        }

        @Test
        @DisplayName("Gemini가 누락한 9자리 학번은 이름/등번호가 null로 담긴다")
        void 자릿수_불일치_Gemini_누락_이름_등번호_null() {
            // given: 10자리 계정인데 Gemini가 9자리 학번을 누락
            given(mockMember.getOrganization().getStudentNumberDigits()).willReturn(10);
            NlParseRequest request = new NlParseRequest(
                    List.of(), "경희일 1234543221 12\n경희이 123456789 2"
            );
            given(nlClient.parsePlayers(anyString(), anyList(), anyInt()))
                    .willReturn(NlParseResult.ofPlayers(List.of(
                            new ParsedPlayer("경희일", "1234543221", 12)
                    )));

            // when
            NlParseResponse response = nlService.parse(request, mockMember);

            // then
            assertThat(response.preview().parseFailedLines())
                    .extracting(NlFailedLine::studentNumber, NlFailedLine::name, NlFailedLine::jerseyNumber)
                    .contains(tuple("123456789", null, null));
        }
    }

    @Nested
    @DisplayName("registerTeamWithPlayers - 팀 생성 + 선수 등록 통합")
    class RegisterTeamWithPlayers {

        @Test
        @DisplayName("팀을 생성하고 선수를 등록한다")
        void 팀_생성_및_선수_등록() {
            // given
            NlRegisterTeamRequest request = new NlRegisterTeamRequest(
                    new NlRegisterTeamRequest.TeamInfo("정치외교학과 DPS", "https://images.hufscheer.com/logo.png", "정치외교학과", "#FF0000", null),
                    List.of(new NlRegisterTeamRequest.PlayerData("홍길동", "202600001", 10))
            );

            given(teamService.registerAndReturnId(any(), any())).willReturn(99L);

            Team createdTeam = mock(Team.class);
            given(createdTeam.getId()).willReturn(99L);
            given(createdTeam.getName()).willReturn("정치외교학과 DPS");
            given(entityUtils.getEntity(99L, Team.class)).willReturn(createdTeam);

            given(teamPlayerRepository.findPlayerIdsByTeamId(99L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(playerService.register(any(), any())).willReturn(100L);

            // when
            NlRegisterTeamResponse response = nlService.registerTeamWithPlayers(request, mockMember);

            // then
            assertThat(response.teamId()).isEqualTo(99L);
            assertThat(response.result().created()).isEqualTo(1);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(teamService).registerAndReturnId(any(), any());
            verify(teamService).addPlayersToTeam(any(), eq(99L), anyList());
        }

        @Test
        @DisplayName("sportType을 지정하면 해당 종목으로 팀이 생성된다")
        void sportType_지정_팀_생성() {
            // given
            NlRegisterTeamRequest request = new NlRegisterTeamRequest(
                    new NlRegisterTeamRequest.TeamInfo("농구팀", "https://images.hufscheer.com/logo.png", "경영대학", "#FF0000", SportType.BASKETBALL),
                    List.of(new NlRegisterTeamRequest.PlayerData("홍길동", "202600001", 10))
            );

            given(teamService.registerAndReturnId(any(), any())).willReturn(99L);

            Team createdTeam = mock(Team.class);
            given(createdTeam.getId()).willReturn(99L);
            given(createdTeam.getName()).willReturn("농구팀");
            given(entityUtils.getEntity(99L, Team.class)).willReturn(createdTeam);

            given(teamPlayerRepository.findPlayerIdsByTeamId(99L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());
            given(playerService.register(any(), any())).willReturn(100L);

            // when
            nlService.registerTeamWithPlayers(request, mockMember);

            // then
            verify(teamService).registerAndReturnId(any(), argThat(register ->
                    register.sportType() == SportType.BASKETBALL
            ));
        }

        @Test
        @DisplayName("기존 선수는 생성하지 않고 팀에 배정한다")
        void 기존_선수_배정() {
            // given
            NlRegisterTeamRequest request = new NlRegisterTeamRequest(
                    new NlRegisterTeamRequest.TeamInfo("정치외교학과 DPS", "https://images.hufscheer.com/logo.png", "정치외교학과", "#FF0000", null),
                    List.of(new NlRegisterTeamRequest.PlayerData("김철수", "202600002", 7))
            );

            given(teamService.registerAndReturnId(any(), any())).willReturn(99L);

            Team createdTeam = mock(Team.class);
            given(createdTeam.getId()).willReturn(99L);
            given(createdTeam.getName()).willReturn("정치외교학과 DPS");
            given(entityUtils.getEntity(99L, Team.class)).willReturn(createdTeam);

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getId()).willReturn(42L);
            given(existingPlayer.getStudentNumber()).willReturn("202600002");
            given(existingPlayer.isManagedBy(mockMember)).willReturn(true);

            given(teamPlayerRepository.findPlayerIdsByTeamId(99L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));

            // when
            NlRegisterTeamResponse response = nlService.registerTeamWithPlayers(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(0);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(playerService, never()).register(any(), any());
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
            given(playerService.register(any(), any())).willReturn(100L);

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(1);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(playerService).register(any(), any());
            verify(teamService).addPlayersToTeam(any(), eq(1L), anyList());
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
            given(existingPlayer.isManagedBy(mockMember)).willReturn(true);

            given(entityUtils.getEntity(1L, Team.class)).willReturn(mockTeam);
            given(teamPlayerRepository.findPlayerIdsByTeamId(1L)).willReturn(List.of());
            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(0);
            assertThat(response.result().assigned()).isEqualTo(1);
            verify(playerService, never()).register(any(), any());
            verify(teamService).addPlayersToTeam(any(), eq(1L), anyList());
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
            verify(teamService, never()).addPlayersToTeam(any(), anyLong(), anyList());
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
            given(playerService.register(any(), any())).willReturn(100L);

            // when
            NlExecuteResponse response = nlService.execute(request, mockMember);

            // then
            assertThat(response.result().created()).isEqualTo(1);
            assertThat(response.result().assigned()).isEqualTo(1);
            assertThat(response.result().skipped()).isEqualTo(1);
            verify(playerService, times(1)).register(any(), any());
        }
    }

    @Nested
    @DisplayName("checkDuplicates - 학번 중복 체크")
    class CheckDuplicates {

        @Test
        @DisplayName("중복 학번이 있으면 EXISTS로 표시한다")
        void 중복_학번_EXISTS() {
            // given
            NlCheckDuplicatesRequest request = new NlCheckDuplicatesRequest(
                    List.of(
                            new NlCheckDuplicatesRequest.PlayerData("홍길동", "202600001", 10),
                            new NlCheckDuplicatesRequest.PlayerData("김철수", "202600002", 7)
                    )
            );

            Player existingPlayer = mock(Player.class);
            given(existingPlayer.getId()).willReturn(42L);
            given(existingPlayer.getStudentNumber()).willReturn("202600001");

            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of(existingPlayer));

            // when
            NlCheckDuplicatesResponse response = nlService.checkDuplicates(request);

            // then
            assertThat(response.players()).hasSize(2);
            assertThat(response.players().get(0).status()).isEqualTo(PlayerStatus.EXISTS);
            assertThat(response.players().get(0).existingPlayerId()).isEqualTo(42L);
            assertThat(response.players().get(1).status()).isEqualTo(PlayerStatus.NEW);
            assertThat(response.players().get(1).existingPlayerId()).isNull();
            assertThat(response.summary().newPlayers()).isEqualTo(1);
            assertThat(response.summary().existingPlayers()).isEqualTo(1);
            assertThat(response.summary().alreadyInTeam()).isEqualTo(0);
        }

        @Test
        @DisplayName("중복 학번이 없으면 전부 NEW로 표시한다")
        void 중복_없으면_전부_NEW() {
            // given
            NlCheckDuplicatesRequest request = new NlCheckDuplicatesRequest(
                    List.of(
                            new NlCheckDuplicatesRequest.PlayerData("홍길동", "202600001", 10)
                    )
            );

            given(playerRepository.findByStudentNumberIn(anyList())).willReturn(List.of());

            // when
            NlCheckDuplicatesResponse response = nlService.checkDuplicates(request);

            // then
            assertThat(response.players()).hasSize(1);
            assertThat(response.players().get(0).status()).isEqualTo(PlayerStatus.NEW);
            assertThat(response.summary().newPlayers()).isEqualTo(1);
            assertThat(response.summary().existingPlayers()).isEqualTo(0);
        }
    }
}
