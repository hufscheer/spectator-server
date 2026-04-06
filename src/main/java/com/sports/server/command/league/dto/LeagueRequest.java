package com.sports.server.command.league.dto;

import com.sports.server.command.league.domain.Round;
import com.sports.server.command.league.domain.SportType;
import java.time.LocalDateTime;
import java.util.List;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;

public class LeagueRequest {
	public record Register(
		String name,
		int maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt,
		List<Long> teamIds,
		SportType sportType
	) {
		public League toEntity(final Member manager) {
			return new League(manager, manager.getOrganization(), name, startAt, endAt, Round.from(maxRound), sportType);
		}
	}

	public record Update(
		String name,
		int maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
	}

	public record Teams(
			List<Long> teamIds
	) {
	}
}
