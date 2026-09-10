package com.sports.server.command.player.exception;

public class PlayerErrorMessages {
    public static final String TEAM_PLAYER_NOT_FOUND_EXCEPTION = "해당 팀선수 정보를 찾을 수 없습니다: ";
    public static final String PLAYER_NOT_EXIST_EXCEPTION = "존재하지 않는 선수입니다.";
    public static final String CANNOT_DELETE_WITH_LINEUPS =
            "경기 라인업에 등록된 선수는 삭제할 수 없습니다. 해당 경기의 라인업에서 먼저 빼주세요.";
}
