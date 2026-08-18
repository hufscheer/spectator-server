package com.sports.server.command.bracket.application;

import com.sports.server.command.bracket.domain.Bracket;
import com.sports.server.command.bracket.domain.BracketMatch;
import com.sports.server.command.bracket.domain.BracketMatchRepository;
import com.sports.server.command.bracket.dto.BracketRequest;
import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueTeam;
import com.sports.server.command.league.domain.LeagueTeamRepository;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.application.PermissionValidator;
import com.sports.server.common.exception.BadRequestException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BracketService {

    private static final int MINIMUM_TEAMS = 2;
    private static final int FIRST_POSITION = 1;

    private final EntityUtils entityUtils;
    private final BracketMatchRepository bracketMatchRepository;
    private final LeagueTeamRepository leagueTeamRepository;

    public void create(final League league, final List<Team> teams, final BracketRequest.Save request) {
        bracketMatchRepository.saveAll(validatedBracketMatches(league, teams, request));
    }

    public void replace(final Member administrator, final Long leagueId, final BracketRequest.Save request) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, administrator);

        if (bracketMatchRepository.existsByLeagueIdAndGameIsNotNull(leagueId)) {
            throw new BadRequestException(BracketErrorMessages.CANNOT_MODIFY_WITH_LINKED_GAMES);
        }

        List<Team> teams = findValidatedLeagueTeams(leagueId, request);
        List<BracketMatch> matches = validatedBracketMatches(league, teams, request);

        bracketMatchRepository.deleteAllByLeagueId(leagueId);
        bracketMatchRepository.saveAll(matches);
    }

    public void linkGame(final League league, final Game game, final Long teamId1, final Long teamId2) {
        List<BracketMatch> matches = bracketMatchRepository.findAllByLeagueId(league.getId());
        if (matches.isEmpty()) {
            return;
        }
        Bracket bracket = Bracket.from(matches);
        // 3·4위전은 결승과 참가 팀 수가 같아 findMeetingMatch 로는 결승 매치가 잡힌다. 트리 조회 전에 분리한다
        if (game.getRound() == Round.THIRD_PLACE_MATCH) {
            Optional.ofNullable(bracket.getThirdPlaceMatch())
                    .filter(match -> !match.isLinked())
                    .ifPresent(match -> match.linkGame(game));
            return;
        }
        bracket.findMeetingMatch(game.getRound(), teamId1, teamId2)
                .filter(match -> !match.isLinked())
                .ifPresent(match -> match.linkGame(game));
    }

    public void validateThirdPlaceContenders(final League league, final Long teamId1, final Long teamId2) {
        List<BracketMatch> matches = bracketMatchRepository.findAllByLeagueId(league.getId());
        if (matches.isEmpty()) {
            return;
        }
        Bracket.from(matches).validateThirdPlaceContenders(teamId1, teamId2);
    }

    public void relinkGame(final League league, final Game game) {
        unlinkGame(game);
        if (game.getGameTeams().size() == Game.MINIMUM_TEAMS) {
            linkGame(league, game, game.getTeam1().getTeam().getId(), game.getTeam2().getTeam().getId());
        }
    }

    public void unlinkGame(final Game game) {
        bracketMatchRepository.findByGame(game).ifPresent(BracketMatch::unlinkGame);
    }

    public void validateThirdPlaceChangeable(final Long leagueId) {
        if (bracketMatchRepository.existsByLeagueIdAndGameIsNotNull(leagueId)) {
            throw new BadRequestException(LeagueErrorMessages.THIRD_PLACE_CANNOT_CHANGE_WITH_LINKED_GAMES);
        }
    }

    public void removeTeams(final League league, final List<Team> teams) {
        bracketMatchRepository.findAllByLeagueId(league.getId()).stream()
                .filter(match -> !match.isLinked())
                .forEach(match -> teams.forEach(match::removeTeam));
    }

    private List<BracketMatch> validatedBracketMatches(final League league, final List<Team> teams,
                                                       final BracketRequest.Save request) {
        validateSize(league, request.size());
        Map<Integer, Team> placements = validatedPlacements(request, teams);
        return Bracket.generate(league, request.size(), placements);
    }

    private void validateSize(final League league, final int size) {
        if (!Round.isValidNumber(size) || size == Round.PRELIMINARY.getNumber()) {
            throw new BadRequestException(BracketErrorMessages.INVALID_BRACKET_SIZE);
        }
        league.validateRoundWithinLimit(size);
    }

    private Map<Integer, Team> validatedPlacements(final BracketRequest.Save request, final List<Team> teams) {
        Map<Long, Team> teamsById = teams.stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));

        Map<Integer, Team> placements = new HashMap<>();
        Set<Long> placedTeamIds = new HashSet<>();
        for (BracketRequest.Entry entry : validatedEntries(request)) {
            if (entry.position() < FIRST_POSITION || entry.position() > request.size()) {
                throw new BadRequestException(BracketErrorMessages.POSITION_OUT_OF_RANGE);
            }
            if (!placedTeamIds.add(entry.teamId())) {
                throw new BadRequestException(BracketErrorMessages.DUPLICATED_TEAM);
            }
            Team team = teamsById.get(entry.teamId());
            if (team == null) {
                throw new BadRequestException(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
            }
            if (placements.putIfAbsent(entry.position(), team) != null) {
                throw new BadRequestException(BracketErrorMessages.DUPLICATED_POSITION);
            }
        }
        return placements;
    }

    private List<Team> findValidatedLeagueTeams(final Long leagueId, final BracketRequest.Save request) {
        List<Long> teamIds = validatedEntries(request).stream()
                .map(BracketRequest.Entry::teamId)
                .distinct()
                .toList();
        List<LeagueTeam> leagueTeams = leagueTeamRepository.findAllByLeagueAndTeamIdsIn(leagueId, teamIds);
        if (leagueTeams.size() != teamIds.size()) {
            throw new BadRequestException(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
        }
        return leagueTeams.stream()
                .map(LeagueTeam::getTeam)
                .toList();
    }

    private List<BracketRequest.Entry> validatedEntries(final BracketRequest.Save request) {
        List<BracketRequest.Entry> entries = request.entries();
        if (entries == null || entries.size() < MINIMUM_TEAMS) {
            throw new BadRequestException(BracketErrorMessages.NOT_ENOUGH_TEAMS);
        }
        return entries;
    }
}
