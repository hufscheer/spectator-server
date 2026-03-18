package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.member.domain.Member;
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

    private static final Pattern NINE_DIGIT_PATTERN = Pattern.compile("(?<!\\d)\\d{9}(?!\\d)");
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z\\s]{1,50}$");

    // --- public API ---

    @Transactional(readOnly = true)
    public NlProcessResponse process(NlProcessRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        NlParseResult parseResult = nlClient.parsePlayers(request.message(), request.history());

        if (!parseResult.parsed()) {
            return new NlProcessResponse(
                    parseResult.textMessage() != null ? parseResult.textMessage() : NlErrorMessages.PARSE_FAILED,
                    null
            );
        }

        if (parseResult.players().isEmpty()) {
            return new NlProcessResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        return buildProcessPreview(request, team, parseResult.players());
    }

    public NlParseResponse parse(NlParseRequest request) {
        NlParseResult parseResult = nlClient.parsePlayers(request.message(), request.history());

        if (!parseResult.parsed()) {
            return new NlParseResponse(
                    parseResult.textMessage() != null ? parseResult.textMessage() : NlErrorMessages.PARSE_FAILED,
                    null
            );
        }

        if (parseResult.players().isEmpty()) {
            return new NlParseResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        return buildParsePreview(request.message(), parseResult.players());
    }

    @Transactional
    public NlRegisterTeamResponse registerTeamWithPlayers(NlRegisterTeamRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);

        Team team = createTeamAndAddToLeague(request, league);

        List<NlExecuteRequest.PlayerData> playerDataList = toExecutePlayerData(request.players());
        ExecuteContext context = buildExecuteContext(team.getId(), playerDataList);
        processPlayersForExecution(playerDataList, context);

        if (!context.teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(team.getId(), context.teamPlayerRegisters);
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

        ExecuteContext context = buildExecuteContext(request.teamId(), request.players());
        processPlayersForExecution(request.players(), context);

        if (!context.teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(request.teamId(), context.teamPlayerRegisters);
        }

        String displayMessage = String.format("%s에 %d명의 선수가 등록되었습니다.", team.getName(), context.assigned);
        return new NlExecuteResponse(displayMessage, new NlExecuteResponse.Result(context.created, context.assigned, context.skipped));
    }

    // --- process 전용 (팀 컨텍스트 포함) ---

    private NlProcessResponse buildProcessPreview(NlProcessRequest request, Team team, List<ParsedPlayer> parsedPlayers) {
        Set<String> originalNineDigits = extractNineDigitNumbers(request.message());
        Set<Long> teamPlayerIdSet = new HashSet<>(teamPlayerRepository.findPlayerIdsByTeamId(request.teamId()));
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(
                parsedPlayers.stream().map(ParsedPlayer::studentNumber).filter(Objects::nonNull).toList()
        );

        List<PlayerPreview> playerPreviews = new ArrayList<>();
        List<NlFailedLine> failedLines = new ArrayList<>();
        classifyWithTeamContext(parsedPlayers, originalNineDigits, teamPlayerIdSet, existingPlayerMap, playerPreviews, failedLines);

        Summary summary = buildSummary(playerPreviews);
        int registrableCount = summary.newPlayers() + summary.existingPlayers();
        String displayMessage = String.format("%s에 %d명의 선수를 등록합니다. 확인해주세요.", team.getName(), registrableCount);

        Preview preview = new Preview(
                "REGISTER_PLAYERS_BULK", request.teamId(), team.getName(),
                playerPreviews, summary, failedLines
        );
        return new NlProcessResponse(displayMessage, preview);
    }

    private void classifyWithTeamContext(List<ParsedPlayer> parsedPlayers, Set<String> originalNineDigits,
                                         Set<Long> teamPlayerIdSet, Map<String, Player> existingPlayerMap,
                                         List<PlayerPreview> playerPreviews, List<NlFailedLine> failedLines) {
        Set<String> seenStudentNumbers = new HashSet<>();

        for (int i = 0; i < parsedPlayers.size(); i++) {
            ParsedPlayer parsed = parsedPlayers.get(i);

            NlFailedLine failedLine = validateParsedPlayer(i, parsed, originalNineDigits);
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

    private NlParseResponse buildParsePreview(String message, List<ParsedPlayer> parsedPlayers) {
        Set<String> originalNineDigits = extractNineDigitNumbers(message);

        List<NlParseResponse.ParsedPlayerPreview> playerPreviews = new ArrayList<>();
        List<NlFailedLine> failedLines = new ArrayList<>();
        classifyWithoutTeamContext(parsedPlayers, originalNineDigits, playerPreviews, failedLines);

        String displayMessage = String.format("%d명의 선수가 인식되었습니다.", playerPreviews.size());
        NlParseResponse.Preview preview = new NlParseResponse.Preview(playerPreviews, playerPreviews.size(), failedLines);
        return new NlParseResponse(displayMessage, preview);
    }

    private void classifyWithoutTeamContext(List<ParsedPlayer> parsedPlayers, Set<String> originalNineDigits,
                                             List<NlParseResponse.ParsedPlayerPreview> playerPreviews,
                                             List<NlFailedLine> failedLines) {
        Set<String> seenStudentNumbers = new HashSet<>();

        for (int i = 0; i < parsedPlayers.size(); i++) {
            ParsedPlayer parsed = parsedPlayers.get(i);

            NlFailedLine failedLine = validateParsedPlayer(i, parsed, originalNineDigits);
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
    }

    // --- registerTeamWithPlayers 전용 ---

    private Team createTeamAndAddToLeague(NlRegisterTeamRequest request, League league) {
        NlRegisterTeamRequest.TeamInfo teamInfo = request.team();
        TeamRequest.Register teamRegister = new TeamRequest.Register(
                teamInfo.name(), teamInfo.logoImageUrl(), teamInfo.unit(), teamInfo.teamColor(), null
        );
        Long teamId = teamService.registerAndReturnId(teamRegister);
        Team team = entityUtils.getEntity(teamId, Team.class);
        leagueTeamRepository.save(LeagueTeam.of(league, team));
        return team;
    }

    private List<NlExecuteRequest.PlayerData> toExecutePlayerData(List<NlRegisterTeamRequest.PlayerData> players) {
        return players.stream()
                .map(p -> new NlExecuteRequest.PlayerData(p.name(), p.studentNumber(), p.jerseyNumber()))
                .toList();
    }

    // --- execute 공용 ---

    private ExecuteContext buildExecuteContext(Long teamId, List<NlExecuteRequest.PlayerData> players) {
        Set<Long> teamPlayerIdSet = new HashSet<>(
                teamPlayerRepository.findPlayerIdsByTeamId(teamId)
        );
        List<String> studentNumbers = players.stream()
                .map(NlExecuteRequest.PlayerData::studentNumber)
                .toList();
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(studentNumbers);
        return new ExecuteContext(teamPlayerIdSet, existingPlayerMap);
    }

    private void processPlayersForExecution(List<NlExecuteRequest.PlayerData> players, ExecuteContext context) {
        for (NlExecuteRequest.PlayerData playerData : players) {
            if (!isValidName(playerData.name()) || !context.seenStudentNumbers.add(playerData.studentNumber())) {
                context.skipped++;
                continue;
            }

            Long playerId = getOrCreatePlayerId(playerData, context);
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

    private Long getOrCreatePlayerId(NlExecuteRequest.PlayerData playerData, ExecuteContext context) {
        Player existingPlayer = context.existingPlayerMap.get(playerData.studentNumber());

        if (existingPlayer != null) {
            if (context.teamPlayerIdSet.contains(existingPlayer.getId())) {
                return null;
            }
            return existingPlayer.getId();
        }

        Long playerId = playerService.register(
                new PlayerRequest.Register(playerData.name(), playerData.studentNumber())
        );
        context.created++;
        return playerId;
    }

    private static class ExecuteContext {
        final Set<Long> teamPlayerIdSet;
        final Map<String, Player> existingPlayerMap;
        final Set<String> seenStudentNumbers = new HashSet<>();
        final Set<Long> assignedPlayerIds = new HashSet<>();
        final List<TeamRequest.TeamPlayerRegister> teamPlayerRegisters = new ArrayList<>();
        int created = 0;
        int assigned = 0;
        int skipped = 0;

        ExecuteContext(Set<Long> teamPlayerIdSet, Map<String, Player> existingPlayerMap) {
            this.teamPlayerIdSet = teamPlayerIdSet;
            this.existingPlayerMap = existingPlayerMap;
        }
    }

    // --- 공용 유틸 ---

    private NlFailedLine validateParsedPlayer(int index, ParsedPlayer parsed, Set<String> originalNineDigits) {
        if (isInvalidStudentNumber(parsed, originalNineDigits)) {
            String reason = StudentNumber.isInvalid(parsed.studentNumber())
                    ? NlErrorMessages.STUDENT_NUMBER_INVALID
                    : NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL;
            return new NlFailedLine(index + 1, parsed.studentNumber(), reason);
        }

        if (!isValidName(parsed.name())) {
            return new NlFailedLine(index + 1, parsed.studentNumber(), NlErrorMessages.INVALID_PLAYER_NAME);
        }

        return null;
    }

    private boolean isInvalidStudentNumber(ParsedPlayer parsed, Set<String> originalNineDigits) {
        return StudentNumber.isInvalid(parsed.studentNumber())
                || !originalNineDigits.contains(parsed.studentNumber());
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

    private Set<String> extractNineDigitNumbers(String text) {
        Set<String> numbers = new HashSet<>();
        Matcher matcher = NINE_DIGIT_PATTERN.matcher(text);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }
}
