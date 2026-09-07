package com.sports.server.command.league.exception;

public class LeagueErrorMessages {
	public static final String ROUND_NOT_FOUND_EXCEPTION = "해당 라운드는 존재하지 않습니다.";
	public static final String PROGRESS_NOT_FOUND_EXCEPTION = "해당 대회 상태는 존재하지 않습니다.";
	public static final String TEAMS_NOT_IN_LEAGUE_TEAM_EXCEPTION = "리그에 참가중이지 않은 팀이 포함되어 있습니다.";
	public static final String SPORT_TYPE_MISMATCH_EXCEPTION = "리그와 동일한 종목의 팀만 추가할 수 있습니다.";
	public static final String THIRD_PLACE_NOT_ENABLED = "3·4위전을 진행하지 않는 대회입니다.";
	public static final String THIRD_PLACE_CANNOT_CHANGE_WITH_LINKED_GAMES = "이미 경기가 등록된 대회는 3·4위전 진행 여부를 변경할 수 없습니다.";
}
