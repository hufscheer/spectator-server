package com.sports.server.command.team.exception;

public class TeamErrorMessages {
    public static final String UNIT_NOT_FOUND_EXCEPTION = "존재하지 않는 단위입니다.";
    public static final String TEAM_IN_GAME_DELETE_EXCEPTION =
            "경기에 편성된 팀은 삭제할 수 없습니다. 해당 경기를 먼저 삭제해 주세요.";
    public static final String TEAM_IN_LEAGUE_DELETE_EXCEPTION =
            "대회에 참가 중인 팀은 삭제할 수 없습니다. 대회 참가 팀에서 먼저 제외해 주세요.";
}
