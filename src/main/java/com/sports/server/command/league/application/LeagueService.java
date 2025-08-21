package com.sports.server.command.league.application;

import com.sports.server.command.league.domain.*;
import com.sports.server.command.league.exception.LeagueErrorMessages;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.common.application.EntityUtils;
import com.sports.server.common.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {

    private final EntityUtils entityUtils;
    private final LeagueRepository leagueRepository;
	private final LeagueTeamRepository leagueTeamRepository;
	private final TeamRepository teamRepository;

	public void register(final Member administrator, final LeagueRequest.Register request) {
		League league = leagueRepository.save(request.toEntity(administrator));
		List<Team> teams = findValidatedTeams(request.teamIds());
		saveLeagueTeams(league, teams);
	}

	public void update(final Member administrator, final LeagueRequest.Update request, final Long leagueId) {
		League league = findValidatedLeague(leagueId, administrator);
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}

    public void delete(final Member administrator, final Long leagueId) {
        League league = findValidatedLeague(leagueId, administrator);
        leagueRepository.delete(league);
    }

	public League addTeams(final Member administrator, final Long leagueId, final LeagueRequest.Teams request) {
		League league = findValidatedLeague(leagueId, administrator);

		List<Long> teamIdsToAdd = request.teamIds();
		List<Team> teams = findValidatedTeams(teamIdsToAdd);

		List<Long> existingTeamIds = leagueTeamRepository.findTeamIdsByLeagueIdAndTeamIdIn(leagueId, teamIdsToAdd);
		List<Team> teamsToAdd = teams.stream()
				.filter(team -> !existingTeamIds.contains(team.getId()))
				.toList();

		if (teamsToAdd.isEmpty()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "추가할 수 있는 팀이 존재하지 않습니다.");
		}
		saveLeagueTeams(league, teamsToAdd);
		return league;
	}

	public void removeTeams(final Member administrator, final Long leagueId, final LeagueRequest.Teams request){
		League league = findValidatedLeague(leagueId, administrator);

		List<Long> teamIdsToRemove = request.teamIds();
		findValidatedTeams(teamIdsToRemove);
		if (teamIdsToRemove == null || teamIdsToRemove.isEmpty()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
		}

		List<LeagueTeam> leagueTeamsToRemove = leagueTeamRepository.findAllByLeagueAndTeamIdsIn(leagueId, teamIdsToRemove);
		if (leagueTeamsToRemove.size() != new HashSet<>(teamIdsToRemove).size()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
		}
		leagueTeamsToRemove.forEach(league::removeLeagueTeam);
	}

	private void saveLeagueTeams(League league, List<Team> teams){
		List<LeagueTeam> leagueTeams = teams.stream()
				.map(team -> LeagueTeam.of(league, team))
				.toList();

		leagueTeamRepository.saveAll(leagueTeams);
	}

	private List<Team> findValidatedTeams(final List<Long> teamIds) {
		if (new HashSet<>(teamIds).size() != teamIds.size()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "요청에 중복된 팀 ID가 포함되어 있습니다.");
		}

		List<Team> teams = teamRepository.findAllById(teamIds);
		if (teams.size() != teamIds.size()) {
			throw new NotFoundException(LeagueErrorMessages.TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION);
		}
		return teams;
	}

	private League findValidatedLeague(final Long leagueId, final Member administrator) {
		League league = entityUtils.getEntity(leagueId, League.class);
		if (!league.isManagedBy(administrator)) {
			throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
		}
		return league;
	}
}
