package com.sports.server.command.league.dto;

import com.sports.server.command.bracket.dto.BracketRequest;
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
		SportType sportType,
		BracketRequest.Save bracket,
		boolean thirdPlaceMatchEnabled,
		Boolean bracketEnabled
	) {
		public League toEntity(final Member manager) {
			return new League(manager, manager.getOrganization(), name, startAt, endAt, Round.from(maxRound), sportType,
				thirdPlaceMatchEnabled, bracketEnabled);
		}
	}

	/**
	 * {@code thirdPlaceMatchEnabled} 와 {@code bracketEnabled} 는 래퍼 타입이다. primitive 로 두면
	 * 클라이언트가 필드를 빼먹었을 때 Jackson 이 false 로 채워, 이름만 고쳐도 설정이 조용히 꺼진다.
	 * null 은 "변경 없음" 이다.
	 */
	public record Update(
		String name,
		int maxRound,
		LocalDateTime startAt,
		LocalDateTime endAt,
		Boolean thirdPlaceMatchEnabled,
		Boolean bracketEnabled
	) {
	}

	public record Teams(
			List<Long> teamIds
	) {
	}
}
