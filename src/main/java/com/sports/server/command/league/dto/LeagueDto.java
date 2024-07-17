package com.sports.server.command.league.dto;

import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;

public class LeagueDto {
	public record RegisterRequest(
		Long organizationId,
		String name,
		Integer maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		public League toEntity(final Member manager, final Organization organization) {
			return new League(manager, organization, name, startAt, endAt, maxRound);
		}
	}
}
