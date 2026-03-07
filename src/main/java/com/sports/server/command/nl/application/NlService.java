package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.dto.NlExecuteRequest;
import com.sports.server.command.nl.dto.NlExecuteResponse;
import com.sports.server.command.nl.dto.NlProcessRequest;
import com.sports.server.command.nl.dto.NlProcessResponse;
import com.sports.server.command.nl.dto.NlProcessResponse.*;
import com.sports.server.command.nl.infra.GeminiFunctionCallResponse;
import com.sports.server.command.nl.infra.NlGeminiClient;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.nl.exception.NlErrorMessages;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.util.StudentNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NlService {

    private final NlGeminiClient nlGeminiClient;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final EntityUtils entityUtils;
    private final PlayerService playerService;
    private final TeamService teamService;

    private static final Pattern NINE_DIGIT_PATTERN = Pattern.compile("(?<!\\d)\\d{9}(?!\\d)");

    @Transactional(readOnly = true)
    public NlProcessResponse process(NlProcessRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        // 1. Gemini Function Calling으로 텍스트 파싱
        GeminiFunctionCallResponse geminiResponse = nlGeminiClient.parsePlayers(
                request.message(), request.history()
        );

        if (!geminiResponse.hasFunctionCall()) {
            return new NlProcessResponse(
                    geminiResponse.getText().isEmpty()
                            ? NlErrorMessages.PARSE_FAILED
                            : geminiResponse.getText(),
                    null
            );
        }

        // 2. Function Call 결과에서 선수 목록 추출
        Map<String, Object> args = geminiResponse.getFunctionCall().args();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parsedPlayers = (List<Map<String, Object>>) args.get("players");

        if (parsedPlayers == null || parsedPlayers.isEmpty()) {
            return new NlProcessResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        // 3. 원본 텍스트에서 9자리 숫자 추출 (학번 대조 검증용)
        Set<String> originalNineDigits = extractNineDigitNumbers(request.message());

        // 4. 각 선수 검증 + status 분류
        List<PlayerPreview> playerPreviews = new ArrayList<>();
        List<FailedLine> failedLines = new ArrayList<>();
        Set<String> seenStudentNumbers = new HashSet<>();

        // 팀에 이미 소속된 선수 ID 조회
        List<Long> existingPlayerIdsInTeam = teamPlayerRepository.findPlayerIdsByTeamId(request.teamId());
        Set<Long> teamPlayerIdSet = new HashSet<>(existingPlayerIdsInTeam);

        // 파싱된 학번으로 DB 일괄 조회
        List<String> parsedStudentNumbers = parsedPlayers.stream()
                .map(p -> (String) p.get("studentNumber"))
                .filter(Objects::nonNull)
                .toList();
        Map<String, Player> existingPlayerMap = playerRepository.findByStudentNumberIn(parsedStudentNumbers)
                .stream()
                .collect(Collectors.toMap(Player::getStudentNumber, p -> p));

        for (int i = 0; i < parsedPlayers.size(); i++) {
            Map<String, Object> parsed = parsedPlayers.get(i);
            String name = (String) parsed.get("name");
            String studentNumber = (String) parsed.get("studentNumber");
            Integer jerseyNumber = parsed.get("jerseyNumber") instanceof Number n ? n.intValue() : null;

            // 학번 형식 + 원본 대조 검증
            if (StudentNumber.isInvalid(studentNumber) || !originalNineDigits.contains(studentNumber)) {
                failedLines.add(new FailedLine(
                        i + 1,
                        name != null ? name + " " + studentNumber : "라인 " + (i + 1),
                        StudentNumber.isInvalid(studentNumber)
                                ? NlErrorMessages.STUDENT_NUMBER_INVALID
                                : NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL
                ));
                continue;
            }

            // 입력 내 중복 체크
            if (seenStudentNumbers.contains(studentNumber)) {
                playerPreviews.add(new PlayerPreview(name, studentNumber, jerseyNumber, "DUPLICATE_IN_INPUT", null));
                continue;
            }
            seenStudentNumbers.add(studentNumber);

            // DB 검증
            Player existingPlayer = existingPlayerMap.get(studentNumber);
            if (existingPlayer == null) {
                playerPreviews.add(new PlayerPreview(name, studentNumber, jerseyNumber, "NEW", null));
            } else if (teamPlayerIdSet.contains(existingPlayer.getId())) {
                playerPreviews.add(new PlayerPreview(name, studentNumber, jerseyNumber, "ALREADY_IN_TEAM", existingPlayer.getId()));
            } else {
                playerPreviews.add(new PlayerPreview(name, studentNumber, jerseyNumber, "EXISTS", existingPlayer.getId()));
            }
        }

        // 5. Summary 생성
        int newCount = (int) playerPreviews.stream().filter(p -> "NEW".equals(p.status())).count();
        int existsCount = (int) playerPreviews.stream().filter(p -> "EXISTS".equals(p.status())).count();
        int alreadyInTeamCount = (int) playerPreviews.stream().filter(p -> "ALREADY_IN_TEAM".equals(p.status())).count();

        Summary summary = new Summary(playerPreviews.size(), newCount, existsCount, alreadyInTeamCount);

        String teamName = team.getName();
        String displayMessage = String.format("%s에 %d명의 선수를 등록합니다. 확인해주세요.", teamName, newCount + existsCount);

        Preview preview = new Preview(
                "REGISTER_PLAYERS_BULK",
                request.teamId(),
                teamName,
                playerPreviews,
                summary,
                failedLines
        );

        return new NlProcessResponse(displayMessage, preview);
    }

    @Transactional
    public NlExecuteResponse execute(NlExecuteRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        Set<Long> teamPlayerIdSet = new HashSet<>(
                teamPlayerRepository.findPlayerIdsByTeamId(request.teamId())
        );

        // 학번으로 기존 선수 일괄 조회 (N+1 방지)
        List<String> studentNumbers = request.players().stream()
                .map(NlExecuteRequest.PlayerData::studentNumber)
                .toList();
        Map<String, Player> existingPlayerMap = playerRepository.findByStudentNumberIn(studentNumbers)
                .stream()
                .collect(Collectors.toMap(Player::getStudentNumber, p -> p));

        int created = 0;
        int assigned = 0;
        int skipped = 0;

        Set<String> seenStudentNumbers = new HashSet<>();
        Set<Long> assignedPlayerIds = new HashSet<>();
        List<TeamRequest.TeamPlayerRegister> teamPlayerRegisters = new ArrayList<>();

        for (NlExecuteRequest.PlayerData playerData : request.players()) {
            // 학번 중복 입력 방지
            if (!seenStudentNumbers.add(playerData.studentNumber())) {
                skipped++;
                continue;
            }

            Player existingPlayer = existingPlayerMap.get(playerData.studentNumber());

            Long playerId;
            if (existingPlayer != null) {
                playerId = existingPlayer.getId();

                // 이미 팀에 소속된 선수는 스킵
                if (teamPlayerIdSet.contains(playerId)) {
                    skipped++;
                    continue;
                }
            } else {
                // 신규 선수 생성
                playerId = playerService.register(
                        new PlayerRequest.Register(playerData.name(), playerData.studentNumber())
                );
                created++;
            }

            // 중복 playerId 방지
            if (!assignedPlayerIds.add(playerId)) {
                skipped++;
                continue;
            }

            teamPlayerRegisters.add(new TeamRequest.TeamPlayerRegister(
                    playerId,
                    playerData.jerseyNumber()
            ));
            assigned++;
        }

        // 팀에 선수 일괄 배정
        if (!teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(request.teamId(), teamPlayerRegisters);
        }

        String teamName = team.getName();
        String displayMessage = String.format("%s에 %d명의 선수가 등록되었습니다.", teamName, assigned);

        return new NlExecuteResponse(displayMessage, new NlExecuteResponse.Result(created, assigned, skipped));
    }

    private void validateTeamBelongsToLeague(League league, Team team) {
        leagueTeamRepository.findByLeagueAndTeam(league, team)
                .orElseThrow(() -> new BadRequestException(NlErrorMessages.TEAM_NOT_IN_LEAGUE));
    }

    private Set<String> extractNineDigitNumbers(String text) {
        Set<String> numbers = new HashSet<>();
        Matcher matcher = NINE_DIGIT_PATTERN.matcher(text);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }
}
