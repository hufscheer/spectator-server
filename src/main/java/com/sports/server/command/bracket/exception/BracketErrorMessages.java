package com.sports.server.command.bracket.exception;

public class BracketErrorMessages {
    public static final String BRACKET_NOT_FOUND = "해당 리그의 대진표가 존재하지 않습니다.";
    public static final String INVALID_BRACKET_SIZE = "유효하지 않은 대진표 크기입니다.";
    public static final String NOT_ENOUGH_TEAMS = "대진표에는 최소 2팀 이상 배치되어야 합니다.";
    public static final String POSITION_OUT_OF_RANGE = "대진표 위치가 유효한 범위를 벗어났습니다.";
    public static final String DUPLICATED_POSITION = "대진표에 중복된 위치가 포함되어 있습니다.";
    public static final String DUPLICATED_TEAM = "대진표에 중복된 팀이 포함되어 있습니다.";
    public static final String CANNOT_MODIFY_WITH_LINKED_GAMES = "이미 경기가 연결된 대진표는 수정할 수 없습니다.";
}
