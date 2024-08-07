package com.sports.server.command.league.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRepository;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.dto.LeagueRequestDto;
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

	public void register(final Member manager, final LeagueRequestDto.Register request) {
		Organization organization = entityUtils.getEntity(request.organizationId(), Organization.class);
		leagueRepository.save(request.toEntity(manager, organization));
	}

	public void update(final Member manager, final LeagueRequestDto.Update request, final Long leagueId) {
		League league = entityUtils.getEntity(leagueId, League.class);
		league.updateInfo(request.name(), request.startAt(), request.endAt(), Round.from(request.maxRound()));
	}
}
