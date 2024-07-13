package com.sports.server.command.game.domain;

import java.util.ArrayList;
import java.util.List;

import com.sports.server.command.leagueteam.domain.LeagueTeam;
import com.sports.server.common.domain.BaseEntity;
import com.sports.server.common.exception.CustomException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.http.HttpStatus;

@Entity
@Getter
@Table(name = "game_teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTeam extends BaseEntity<GameTeam> {

	private static final int MAXIMUM_OF_CHEER_COUNT = 500;
	private static final int MAXIMUM_OF_TOTAL_CHEER_COUNT = 100_000_000;
	private static final int MINIMUM_OF_CHEER_COUNT = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id")
	private Game game;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "league_team_id")
	private LeagueTeam leagueTeam;

	@OneToMany(mappedBy = "gameTeam")
	private List<LineupPlayer> lineupPlayers = new ArrayList<>();

	@Column(name = "cheer_count", nullable = false)
	private int cheerCount;

	@Column(name = "score", nullable = false)
	private int score;

	public void validateCheerCountOfGameTeam(final int cheerCount) {
		if (cheerCount >= MAXIMUM_OF_CHEER_COUNT || cheerCount <= MINIMUM_OF_CHEER_COUNT) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 범위의 응원 요청 횟수입니다.");
		}
		if (this.cheerCount + cheerCount > MAXIMUM_OF_TOTAL_CHEER_COUNT) {
			throw new CustomException(HttpStatus.BAD_REQUEST, "총 응원 횟수가 한계에 도달했습니다.");
		}
	}

	public boolean matchGame(final Game game) {
		return this.game.equals(game);
	}

	public void registerStarter(final LineupPlayer lineupPlayer) {
		this.lineupPlayers.stream()
			.filter(lp -> lp.equals(lineupPlayer))
			.findAny()
			.ifPresent(LineupPlayer::changeStateToStarter);
	}

	public void rollbackToCandidate(final LineupPlayer lineupPlayer) {
		this.lineupPlayers.stream()
			.filter(lp -> lp.equals(lineupPlayer))
			.findAny()
			.ifPresent(LineupPlayer::changeStateToCandidate);
	}

	public void score(Integer score) {
		this.score += score;
	}
}



