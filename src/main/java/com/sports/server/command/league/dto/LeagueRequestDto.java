package com.sports.server.command.league.dto;

import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.LeagueRound;
import com.sports.server.command.member.domain.Member;
import com.sports.server.command.organization.domain.Organization;

public class LeagueRequestDto {
	public record Register(
		Long organizationId,
		String name,
		String maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		public League toEntity(final Member manager, final Organization organization) {
			return new League(manager, organization, name, startAt, endAt, LeagueRound.from(maxRound));
		}
	}
}
