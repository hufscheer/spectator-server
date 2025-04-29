package com.sports.server.command.game.exception;

public class GameErrorMessages {
    public static final String STATE_NOT_FOUND_EXCEPTION = "해당 경기의 상태는 존재하지 않습니다.";
    public static final String GAME_TEAM_NOT_PARTICIPANT_EXCEPTION = "해당 게임팀은 이 게임에 포함되지 않습니다.";
    public static final String NOT_PARTICIPATING_PLAYER_SCORE = "참여하지 않는 선수는 득점할 수 없습니다.";
    public static final String NOT_PARTICIPATING_PLAYER_PK_SCORE = "참여하지 않는 선수는 승부차기에서 득점할 수 없습니다.";
    public static final String NOT_PARTICIPATING_PLAYER_CANCEL_SCORE = "참여하지 않는 선수는 득점을 취소할 수 없습니다.";
    public static final String NOT_PARTICIPATING_PLAYER_ISSUE_WARNING_CARD = "참여하지 않는 선수는 경고카드를 받을 수 없습니다.";
    public static final String NOT_PARTICIPATING_PLAYER_CANCEL_WARNING_CARD = "참여하지 않는 선수는 경고카드를 취소할 수 없습니다.";
}
