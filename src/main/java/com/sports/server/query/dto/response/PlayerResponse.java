package com.sports.server.query.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.TeamPlayer;
import com.sports.server.common.util.StudentNumber;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlayerResponse(
	Long playerId,
	Long teamPlayerId,
	String name,
	String studentNumber,
	String admissionYear,
	Integer jerseyNumber,
	Integer totalGoalCount,
	List<TeamResponse> teams
) {
	private static final int VISIBLE_STUDENT_NUMBER_LENGTH = 4;

	public PlayerResponse(final Player player, final Long teamPlayerId) {
		this(
				player.getId(),
				teamPlayerId,
				player.getName(),
				player.getStudentNumber(),
				StudentNumber.extractAdmissionYear(player.getStudentNumber()),
				null,
				0,
				Collections.emptyList()
		);
	}

	public static PlayerResponse of(final Player player, final Long teamPlayerId, final int totalGoalCount,
									final List<TeamResponse> teams, final boolean revealStudentNumber) {
		return new PlayerResponse(
				player.getId(),
				teamPlayerId,
				player.getName(),
				studentNumberFor(player, revealStudentNumber),
				StudentNumber.extractAdmissionYear(player.getStudentNumber()),
				null,
				totalGoalCount,
				teams
		);
	}

	public static PlayerResponse of(final TeamPlayer teamPlayer, final int totalGoalCount,
									final boolean revealStudentNumber) {
		Player player = teamPlayer.getPlayer();
		return new PlayerResponse(
				player.getId(),
				teamPlayer.getId(),
				player.getName(),
				studentNumberFor(player, revealStudentNumber),
				StudentNumber.extractAdmissionYear(player.getStudentNumber()),
				teamPlayer.getJerseyNumber(),
				totalGoalCount,
				null
		);
	}

	/** 관리 권한이 없으면 앞 4자리만 남기고 나머지를 '*' 로 가린다(길이 유지). 4자리 이하는 내보내지 않는다. */
	private static String studentNumberFor(final Player player, final boolean reveal) {
		String studentNumber = player.getStudentNumber();
		if (reveal || studentNumber == null) {
			return studentNumber;
		}
		if (studentNumber.length() <= VISIBLE_STUDENT_NUMBER_LENGTH) {
			return null;
		}
		return studentNumber.substring(0, VISIBLE_STUDENT_NUMBER_LENGTH)
				+ "*".repeat(studentNumber.length() - VISIBLE_STUDENT_NUMBER_LENGTH);
	}
}
