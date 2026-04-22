package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.command.nl.domain.PlayerStatus;
import com.sports.server.command.nl.dto.*;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;
import com.sports.server.command.nl.dto.NlProcessResponse.*;
import com.sports.server.command.nl.exception.NlErrorMessages;
import com.sports.server.command.player.application.PlayerService;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.player.domain.PlayerRepository;
import com.sports.server.command.player.dto.PlayerRequest;
import com.sports.server.command.team.application.TeamService;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamPlayerRepository;
import com.sports.server.command.team.dto.TeamRequest;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.ExceptionMessages;
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

    private final NlClient nlClient;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final EntityUtils entityUtils;
    private final PlayerService playerService;
    private final TeamService teamService;

    private static final Pattern STUDENT_NUMBER_PATTERN = Pattern.compile("(?<!\\d)\\d{9,10}(?!\\d)");
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]{1,50}$");

    // --- public API ---

    @Transactional(readOnly = true)
    public NlProcessResponse process(NlProcessRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        int studentNumberDigits = member.getOrganization().getStudentNumberDigits();
        NlParseResult parseResult = nlClient.parsePlayers(request.message(), request.history(), studentNumberDigits);

        if (!parseResult.parsed()) {
            return new NlProcessResponse(
                    parseResult.textMessage() != null ? parseResult.textMessage() : NlErrorMessages.PARSE_FAILED,
                    null
            );
        }

        if (parseResult.players().isEmpty()) {
            return new NlProcessResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        return buildProcessPreview(request, team, parseResult.players(), member.getOrganization());
    }

    @Transactional(readOnly = true)
    public NlParseResponse parse(NlParseRequest request, Member member) {
        int studentNumberDigits = member.getOrganization().getStudentNumberDigits();
        NlParseResult parseResult = nlClient.parsePlayers(request.message(), request.history(), studentNumberDigits);

        if (!parseResult.parsed()) {
            return new NlParseResponse(
                    parseResult.textMessage() != null ? parseResult.textMessage() : NlErrorMessages.PARSE_FAILED,
                    null
            );
        }

        if (parseResult.players().isEmpty()) {
            return new NlParseResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        return buildParsePreview(request.message(), parseResult.players(), studentNumberDigits);
    }

    @Transactional
    public NlRegisterTeamResponse registerTeamWithPlayers(NlRegisterTeamRequest request, Member member) {
        Organization organization = member.getOrganization();
        Team team = createTeam(request, member);

        List<NlExecuteRequest.PlayerData> playerDataList = toExecutePlayerData(request.players());
        ExecuteContext context = buildExecuteContext(team.getId(), playerDataList, organization);
        processPlayersForExecution(playerDataList, context, member);

        if (!context.teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(member, team.getId(), context.teamPlayerRegisters);
        }

        String displayMessage = String.format("%s에 %d명의 선수가 등록되었습니다.", team.getName(), context.assigned);
        return new NlRegisterTeamResponse(
                displayMessage, team.getId(),
                new NlRegisterTeamResponse.Result(context.created, context.assigned, context.skipped)
        );
    }

    @Transactional
    public NlExecuteResponse execute(NlExecuteRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        Organization organization = member.getOrganization();
        ExecuteContext context = buildExecuteContext(request.teamId(), request.players(), organization);
        processPlayersForExecution(request.players(), context, member);

        if (!context.teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(member, request.teamId(), context.teamPlayerRegisters);
        }

        String displayMessage = String.format("%s에 %d명의 선수가 등록되었습니다.", team.getName(), context.assigned);
        return new NlExecuteResponse(displayMessage, new NlExecuteResponse.Result(context.created, context.assigned, context.skipped));
    }

    // --- process 전용 (팀 컨텍스트 포함) ---

    private NlProcessResponse buildProcessPreview(NlProcessRequest request, Team team, List<ParsedPlayer> parsedPlayers, Organization organization) {
        Map<String, Integer> originalStudentNumberLineMap = extractStudentNumberLineMap(request.message());
        Set<Long> teamPlayerIdSet = new HashSet<>(teamPlayerRepository.findPlayerIdsByTeamId(request.teamId()));
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(
                parsedPlayers.stream().map(ParsedPlayer::studentNumber).filter(Objects::nonNull).toList()
        );

        List<PlayerPreview> playerPreviews = new ArrayList<>();
        List<NlFailedLine> failedLines = new ArrayList<>();
        classifyWithTeamContext(parsedPlayers, originalStudentNumberLineMap, teamPlayerIdSet, existingPlayerMap, playerPreviews, failedLines, organization.getStudentNumberDigits());

        Summary summary = buildSummary(playerPreviews);
        int registrableCount = summary.newPlayers() + summary.existingPlayers();
        String displayMessage = String.format("%s에 %d명의 선수를 등록합니다. 확인해주세요.", team.getName(), registrableCount);

        Preview preview = new Preview(
                "REGISTER_PLAYERS_BULK", request.teamId(), team.getName(),
                playerPreviews, summary, failedLines
        );
        return new NlProcessResponse(displayMessage, preview);
    }

    private void classifyWithTeamContext(List<ParsedPlayer> parsedPlayers, Map<String, Integer> originalStudentNumberLineMap,
                                         Set<Long> teamPlayerIdSet, Map<String, Player> existingPlayerMap,
                                         List<PlayerPreview> playerPreviews, List<NlFailedLine> failedLines,
                                         int studentNumberDigits) {
        Set<String> seenStudentNumbers = new HashSet<>();

        for (int i = 0; i < parsedPlayers.size(); i++) {
            ParsedPlayer parsed = parsedPlayers.get(i);

            NlFailedLine failedLine = validateParsedPlayer(i, parsed, originalStudentNumberLineMap, studentNumberDigits);
            if (failedLine != null) {
                failedLines.add(failedLine);
                continue;
            }

            if (!seenStudentNumbers.add(parsed.studentNumber())) {
                continue;
            }

            Player existingPlayer = existingPlayerMap.get(parsed.studentNumber());
            playerPreviews.add(classifyPlayer(parsed, existingPlayer, teamPlayerIdSet));
        }

        addDigitMismatchFailures(originalStudentNumberLineMap, studentNumberDigits, failedLines);
    }

    private PlayerPreview classifyPlayer(ParsedPlayer parsed, Player existingPlayer, Set<Long> teamPlayerIdSet) {
        if (existingPlayer == null) {
            return toPlayerPreview(parsed, PlayerStatus.NEW, null);
        }
        if (teamPlayerIdSet.contains(existingPlayer.getId())) {
            return toPlayerPreview(parsed, PlayerStatus.ALREADY_IN_TEAM, existingPlayer.getId());
        }
        return toPlayerPreview(parsed, PlayerStatus.EXISTS, existingPlayer.getId());
    }

    private PlayerPreview toPlayerPreview(ParsedPlayer parsed, PlayerStatus status, Long existingPlayerId) {
        return new PlayerPreview(
                parsed.name(), parsed.studentNumber(), parsed.jerseyNumber(),
                status, existingPlayerId
        );
    }

    private Summary buildSummary(List<PlayerPreview> playerPreviews) {
        int newCount = 0, existsCount = 0, alreadyInTeamCount = 0;
        for (PlayerPreview p : playerPreviews) {
            switch (p.status()) {
                case NEW -> newCount++;
                case EXISTS -> existsCount++;
                case ALREADY_IN_TEAM -> alreadyInTeamCount++;
            }
        }
        return new Summary(playerPreviews.size(), newCount, existsCount, alreadyInTeamCount);
    }

    // --- parse 전용 (팀 컨텍스트 없음) ---

    private NlParseResponse buildParsePreview(String message, List<ParsedPlayer> parsedPlayers, int studentNumberDigits) {
        Map<String, Integer> originalStudentNumberLineMap = extractStudentNumberLineMap(message);

        List<NlParseResponse.ParsedPlayerPreview> playerPreviews = new ArrayList<>();
        List<NlFailedLine> failedLines = new ArrayList<>();
        classifyWithoutTeamContext(parsedPlayers, originalStudentNumberLineMap, playerPreviews, failedLines, studentNumberDigits);

        String displayMessage = String.format("%d명의 선수가 인식되었습니다.", playerPreviews.size());
        NlParseResponse.Preview preview = new NlParseResponse.Preview(playerPreviews, playerPreviews.size(), failedLines);
        return new NlParseResponse(displayMessage, preview);
    }

    private void classifyWithoutTeamContext(List<ParsedPlayer> parsedPlayers, Map<String, Integer> originalStudentNumberLineMap,
                                             List<NlParseResponse.ParsedPlayerPreview> playerPreviews,
                                             List<NlFailedLine> failedLines, int studentNumberDigits) {
        Set<String> seenStudentNumbers = new HashSet<>();

        for (int i = 0; i < parsedPlayers.size(); i++) {
            ParsedPlayer parsed = parsedPlayers.get(i);

            NlFailedLine failedLine = validateParsedPlayer(i, parsed, originalStudentNumberLineMap, studentNumberDigits);
            if (failedLine != null) {
                failedLines.add(failedLine);
                continue;
            }

            if (!seenStudentNumbers.add(parsed.studentNumber())) {
                continue;
            }

            playerPreviews.add(new NlParseResponse.ParsedPlayerPreview(
                    parsed.name(), parsed.studentNumber(), parsed.jerseyNumber()
            ));
        }

        addDigitMismatchFailures(originalStudentNumberLineMap, studentNumberDigits, failedLines);
    }

    @Transactional(readOnly = true)
    public NlCheckDuplicatesResponse checkDuplicates(NlCheckDuplicatesRequest request) {
        List<String> studentNumbers = request.players().stream()
                .map(NlCheckDuplicatesRequest.PlayerData::studentNumber)
                .toList();
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(studentNumbers);

        Set<String> seenStudentNumbers = new HashSet<>();
        List<PlayerPreview> playerPreviews = request.players().stream()
                .filter(p -> seenStudentNumbers.add(p.studentNumber()))
                .map(p -> classifyPlayer(
                        new ParsedPlayer(p.name(), p.studentNumber(), p.jerseyNumber()),
                        existingPlayerMap.get(p.studentNumber()),
                        Collections.emptySet()))
                .toList();

        Summary summary = buildSummary(playerPreviews);
        return new NlCheckDuplicatesResponse(playerPreviews, summary);
    }

    // --- registerTeamWithPlayers 전용 ---

    private Team createTeam(NlRegisterTeamRequest request, Member member) {
        NlRegisterTeamRequest.TeamInfo teamInfo = request.team();
        TeamRequest.Register teamRegister = new TeamRequest.Register(
                teamInfo.name(), teamInfo.logoImageUrl(), teamInfo.unit(), teamInfo.teamColor(), null, null
        );
        Long teamId = teamService.registerAndReturnId(member, teamRegister);
        return entityUtils.getEntity(teamId, Team.class);
    }

    private List<NlExecuteRequest.PlayerData> toExecutePlayerData(List<NlRegisterTeamRequest.PlayerData> players) {
        return players.stream()
                .map(p -> new NlExecuteRequest.PlayerData(p.name(), p.studentNumber(), p.jerseyNumber()))
                .toList();
    }

    // --- execute 공용 ---

    private ExecuteContext buildExecuteContext(Long teamId, List<NlExecuteRequest.PlayerData> players, Organization organization) {
        Set<Long> teamPlayerIdSet = new HashSet<>(
                teamPlayerRepository.findPlayerIdsByTeamId(teamId)
        );
        List<String> studentNumbers = players.stream()
                .map(NlExecuteRequest.PlayerData::studentNumber)
                .toList();
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(studentNumbers);
        return new ExecuteContext(teamPlayerIdSet, existingPlayerMap, organization);
    }

    private void processPlayersForExecution(List<NlExecuteRequest.PlayerData> players, ExecuteContext context, Member member) {
        for (NlExecuteRequest.PlayerData playerData : players) {
            if (!isValidName(playerData.name()) || !context.seenStudentNumbers.add(playerData.studentNumber())) {
                context.skipped++;
                continue;
            }

            Long playerId = getOrCreatePlayerId(playerData, context, member);
            if (playerId == null) {
                context.skipped++;
                continue;
            }

            if (!context.assignedPlayerIds.add(playerId)) {
                context.skipped++;
                continue;
            }

            context.teamPlayerRegisters.add(new TeamRequest.TeamPlayerRegister(playerId, playerData.jerseyNumber()));
            context.assigned++;
        }
    }

    private Long getOrCreatePlayerId(NlExecuteRequest.PlayerData playerData, ExecuteContext context, Member member) {
        Player existingPlayer = context.existingPlayerMap.get(playerData.studentNumber());

        if (existingPlayer != null) {
            if (context.teamPlayerIdSet.contains(existingPlayer.getId())) {
                return null;
            }
            PermissionValidator.checkPermission(existingPlayer, member);
            return existingPlayer.getId();
        }

        Long playerId = playerService.register(
                member, new PlayerRequest.Register(playerData.name(), playerData.studentNumber())
        );
        context.created++;
        return playerId;
    }

    private static class ExecuteContext {
        final Set<Long> teamPlayerIdSet;
        final Map<String, Player> existingPlayerMap;
        final Organization organization;
        final Set<String> seenStudentNumbers = new HashSet<>();
        final Set<Long> assignedPlayerIds = new HashSet<>();
        final List<TeamRequest.TeamPlayerRegister> teamPlayerRegisters = new ArrayList<>();
        int created = 0;
        int assigned = 0;
        int skipped = 0;

        ExecuteContext(Set<Long> teamPlayerIdSet, Map<String, Player> existingPlayerMap, Organization organization) {
            this.teamPlayerIdSet = teamPlayerIdSet;
            this.existingPlayerMap = existingPlayerMap;
            this.organization = organization;
        }
    }

    // --- 공용 유틸 ---

    private NlFailedLine validateParsedPlayer(int index, ParsedPlayer parsed, Map<String, Integer> originalStudentNumberLineMap, int digits) {
        int lineIndex = originalStudentNumberLineMap.getOrDefault(parsed.studentNumber(), index + 1);
        if (StudentNumber.isInvalid(parsed.studentNumber(), digits)) {
            return new NlFailedLine(lineIndex, parsed.studentNumber(), parsed.name(), parsed.jerseyNumber(),
                    String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, digits));
        }
        if (!originalStudentNumberLineMap.containsKey(parsed.studentNumber())) {
            return new NlFailedLine(lineIndex, parsed.studentNumber(), parsed.name(), parsed.jerseyNumber(),
                    NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL);
        }
        if (!isValidName(parsed.name())) {
            return new NlFailedLine(lineIndex, parsed.studentNumber(), parsed.name(), parsed.jerseyNumber(),
                    NlErrorMessages.INVALID_PLAYER_NAME);
        }
        return null;
    }

    private void addDigitMismatchFailures(Map<String, Integer> originalStudentNumberLineMap, int digits, List<NlFailedLine> failedLines) {
        Set<String> alreadyReported = failedLines.stream()
                .map(NlFailedLine::studentNumber)
                .collect(Collectors.toSet());

        for (Map.Entry<String, Integer> entry : originalStudentNumberLineMap.entrySet()) {
            String studentNumber = entry.getKey();
            if (studentNumber.length() == digits) {
                continue;
            }
            if (!alreadyReported.add(studentNumber)) {
                continue;
            }
            failedLines.add(new NlFailedLine(
                    entry.getValue(),
                    studentNumber,
                    null,
                    null,
                    String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, digits)
            ));
        }
    }

    private Map<String, Player> findExistingPlayerMap(List<String> studentNumbers) {
        return playerRepository.findByStudentNumberIn(studentNumbers)
                .stream()
                .collect(Collectors.toMap(Player::getStudentNumber, p -> p));
    }

    private boolean isValidName(String name) {
        return name != null && VALID_NAME_PATTERN.matcher(name).matches();
    }

    private void validateTeamBelongsToLeague(League league, Team team) {
        leagueTeamRepository.findByLeagueAndTeam(league, team)
                .orElseThrow(() -> new BadRequestException(NlErrorMessages.TEAM_NOT_IN_LEAGUE));
    }

    private Map<String, Integer> extractStudentNumberLineMap(String text) {
        Map<String, Integer> lineMap = new LinkedHashMap<>();
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = STUDENT_NUMBER_PATTERN.matcher(lines[i]);
            while (matcher.find()) {
                lineMap.putIfAbsent(matcher.group(), i + 1);
            }
        }
        return lineMap;
    }
}
