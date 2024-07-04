package com.sports.server.command.league.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.dto.LeagueRequest;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;
import com.sports.server.common.application.EntityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueService {
	private final EntityUtils entityUtils;
	private final LeagueRepository leagueRepository;

	public void register(final Member manager, final Long organizationId, final LeagueRequest leagueRequest) {
		Organization organization = entityUtils.getEntity(organizationId, Organization.class);
		leagueRepository.save(
			new League(manager, organization, leagueRequest.name(), leagueRequest.startAt(), leagueRequest.endAt(),
				leagueRequest.maxRound()));
	}
}
