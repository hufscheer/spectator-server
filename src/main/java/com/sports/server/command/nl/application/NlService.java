package com.sports.server.command.nl.application;

import com.sports.server.command.league.domain.League;
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

    @Transactional(readOnly = true)
    public NlProcessResponse process(NlProcessRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        NlParseResult parseResult = nlClient.parsePlayers(request.message(), request.history());

        if (!parseResult.parsed()) {
            if (parseResult.textMessage() != null) {
                return new NlProcessResponse(parseResult.textMessage(), null);
            }
            return new NlProcessResponse(NlErrorMessages.PARSE_FAILED, null);
        }

        if (parseResult.players().isEmpty()) {
            return new NlProcessResponse(NlErrorMessages.NO_PLAYER_INFO, null);
        }

        return buildPreview(request, team, parseResult.players());
    }

    @Transactional
    public NlExecuteResponse execute(NlExecuteRequest request, Member member) {
        League league = entityUtils.getEntity(request.leagueId(), League.class);
        PermissionValidator.checkPermission(league, member);
        Team team = entityUtils.getEntity(request.teamId(), Team.class);
        validateTeamBelongsToLeague(league, team);

        ExecuteContext context = buildExecuteContext(request);
        processPlayersForExecution(request.players(), context);

        if (!context.teamPlayerRegisters.isEmpty()) {
            teamService.addPlayersToTeam(request.teamId(), context.teamPlayerRegisters);
        }

        String displayMessage = String.format("%s에 %d명의 선수가 등록되었습니다.", team.getName(), context.assigned);
        return new NlExecuteResponse(displayMessage, new NlExecuteResponse.Result(context.created, context.assigned, context.skipped));
    }

    private ExecuteContext buildExecuteContext(NlExecuteRequest request) {
        Set<Long> teamPlayerIdSet = new HashSet<>(
                teamPlayerRepository.findPlayerIdsByTeamId(request.teamId())
        );
        List<String> studentNumbers = request.players().stream()
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

    private NlProcessResponse buildPreview(NlProcessRequest request, Team team, List<ParsedPlayer> parsedPlayers) {
        Set<String> originalNineDigits = extractNineDigitNumbers(request.message());
        Set<Long> teamPlayerIdSet = new HashSet<>(teamPlayerRepository.findPlayerIdsByTeamId(request.teamId()));
        Map<String, Player> existingPlayerMap = findExistingPlayerMap(
                parsedPlayers.stream().map(ParsedPlayer::studentNumber).filter(Objects::nonNull).toList()
        );

        List<PlayerPreview> playerPreviews = new ArrayList<>();
        List<FailedLine> failedLines = new ArrayList<>();
        classifyParsedPlayers(parsedPlayers, originalNineDigits, teamPlayerIdSet, existingPlayerMap, playerPreviews, failedLines);

        Summary summary = buildSummary(playerPreviews);
        int registrableCount = summary.newPlayers() + summary.existingPlayers();
        String displayMessage = String.format("%s에 %d명의 선수를 등록합니다. 확인해주세요.", team.getName(), registrableCount);

        Preview preview = new Preview(
                "REGISTER_PLAYERS_BULK", request.teamId(), team.getName(),
                playerPreviews, summary, failedLines
        );
        return new NlProcessResponse(displayMessage, preview);
    }

    private void classifyParsedPlayers(List<ParsedPlayer> parsedPlayers, Set<String> originalNineDigits,
                                       Set<Long> teamPlayerIdSet, Map<String, Player> existingPlayerMap,
                                       List<PlayerPreview> playerPreviews, List<FailedLine> failedLines) {
        Set<String> seenStudentNumbers = new HashSet<>();

        for (int i = 0; i < parsedPlayers.size(); i++) {
            ParsedPlayer parsed = parsedPlayers.get(i);

            if (isInvalidStudentNumber(parsed, originalNineDigits)) {
                failedLines.add(buildFailedLine(i, parsed));
                continue;
            }

            if (!isValidName(parsed.name())) {
                failedLines.add(new FailedLine(i + 1, parsed.studentNumber(), NlErrorMessages.INVALID_PLAYER_NAME));
                continue;
            }

            if (!seenStudentNumbers.add(parsed.studentNumber())) {
                continue;
            }

            Player existingPlayer = existingPlayerMap.get(parsed.studentNumber());
            playerPreviews.add(classifyPlayer(parsed, existingPlayer, teamPlayerIdSet));
        }
    }

    private boolean isInvalidStudentNumber(ParsedPlayer parsed, Set<String> originalNineDigits) {
        return StudentNumber.isInvalid(parsed.studentNumber())
                || !originalNineDigits.contains(parsed.studentNumber());
    }

    private FailedLine buildFailedLine(int index, ParsedPlayer parsed) {
        String reason = StudentNumber.isInvalid(parsed.studentNumber())
                ? NlErrorMessages.STUDENT_NUMBER_INVALID
                : NlErrorMessages.STUDENT_NUMBER_NOT_IN_ORIGINAL;
        return new FailedLine(index + 1, parsed.studentNumber(), reason);
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
        int newCount = (int) playerPreviews.stream().filter(p -> p.status() == PlayerStatus.NEW).count();
        int existsCount = (int) playerPreviews.stream().filter(p -> p.status() == PlayerStatus.EXISTS).count();
        int alreadyInTeamCount = (int) playerPreviews.stream().filter(p -> p.status() == PlayerStatus.ALREADY_IN_TEAM).count();
        return new Summary(playerPreviews.size(), newCount, existsCount, alreadyInTeamCount);
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
