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

    private final EntityUtils entityUtils;
    private final BracketMatchRepository bracketMatchRepository;
    private final LeagueTeamRepository leagueTeamRepository;

    /**
     * 대회 생성 시 대진표를 생성한다. 팀 목록은 LeagueService 에서 이미 검증된 리그 참가 팀이다.
     */
    public void create(final League league, final List<Team> teams, final BracketRequest.Save request) {
        validateAndGenerate(league, teams, request);
    }

    /**
     * 대진표를 새로 배치한다. 아직 대진표가 없던 리그에도 사용할 수 있으며,
     * 이미 경기가 연결된 대진표는 수정할 수 없다.
     */
    public void replace(final Member administrator, final Long leagueId, final BracketRequest.Save request) {
        League league = entityUtils.getEntity(leagueId, League.class);
        PermissionValidator.checkPermission(league, administrator);

        if (bracketMatchRepository.existsByLeagueIdAndGameIsNotNull(leagueId)) {
            throw new BadRequestException(BracketErrorMessages.CANNOT_MODIFY_WITH_LINKED_GAMES);
        }

        List<Team> teams = findValidatedLeagueTeams(leagueId, request);
        bracketMatchRepository.deleteAllByLeagueId(leagueId);
        validateAndGenerate(league, teams, request);
    }

    /**
     * 경기의 두 팀이 대진표상 해당 라운드에서 만나는 매치를 찾아 자동으로 연결한다.
     * 대진표가 없거나, 팀이 배치되지 않았거나, 대진표상 만날 수 없는 조합이면 연결 없이 넘어간다.
     */
    public void linkGame(final League league, final Game game, final Long teamId1, final Long teamId2) {
        List<BracketMatch> matches = bracketMatchRepository.findAllByLeagueId(league.getId());
        if (matches.isEmpty()) {
            return;
        }
        Bracket.from(matches).findMeetingMatch(game.getRound(), teamId1, teamId2)
                .filter(match -> !match.isLinked())
                .ifPresent(match -> match.linkGame(game));
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

    /**
     * 리그에서 제외된 팀을 아직 경기가 연결되지 않은 매치의 배치에서 제거한다.
     */
    public void removeTeams(final League league, final List<Team> teams) {
        bracketMatchRepository.findAllByLeagueId(league.getId()).stream()
                .filter(match -> !match.isLinked())
                .forEach(match -> teams.forEach(match::removeTeam));
    }

    private void validateAndGenerate(final League league, final List<Team> teams,
                                     final BracketRequest.Save request) {
        validateSize(league, request.size());
        Map<Integer, Team> placements = validatedPlacements(request, teams);
        bracketMatchRepository.saveAll(Bracket.generate(league, request.size(), placements));
    }

    private void validateSize(final League league, final int size) {
        if (!Round.isValidNumber(size) || size == Round.PRELIMINARY.getNumber()) {
            throw new BadRequestException(BracketErrorMessages.INVALID_BRACKET_SIZE);
        }
        league.validateRoundWithinLimit(size);
    }

    private Map<Integer, Team> validatedPlacements(final BracketRequest.Save request, final List<Team> teams) {
        List<BracketRequest.Entry> entries = request.entries();
        if (entries == null || entries.size() < 2) {
            throw new BadRequestException(BracketErrorMessages.NOT_ENOUGH_TEAMS);
        }

        Map<Long, Team> teamsById = teams.stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));

        Map<Integer, Team> placements = new HashMap<>();
        Set<Long> placedTeamIds = new HashSet<>();
        for (BracketRequest.Entry entry : entries) {
            if (entry.position() < 1 || entry.position() > request.size()) {
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
        if (request.entries() == null || request.entries().size() < 2) {
            throw new BadRequestException(BracketErrorMessages.NOT_ENOUGH_TEAMS);
        }
        List<Long> teamIds = request.entries().stream()
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
}
