package com.sports.server.command.league.dto;

import com.sports.server.command.league.domain.Round;
import java.time.LocalDateTime;

import com.sports.server.command.league.domain.League;
import com.sports.server.command.member.domain.Member;

public class LeagueRequestDto {
	public record Register(
		String name,
		int maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		public League toEntity(final Member manager) {
			return new League(manager, manager.getOrganization(), name, startAt, endAt, Round.from(maxRound));
		}
	}

	public record Update(
		String name,
		LocalDateTime startAt,
		LocalDateTime endAt,
		int maxRound
	) {
	}
}
