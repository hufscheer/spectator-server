package com.sports.server.command.game.exception;

public class GameErrorMessages {
    public static final String GAME_NOT_FOUND_EXCEPTION = "해당 경기는 존재하지 않습니다.";
    public static final String STATE_NOT_FOUND_EXCEPTION = "해당 경기의 상태는 존재하지 않습니다.";
    public static final String GAME_TEAM_NOT_PARTICIPANT_EXCEPTION = "해당 게임팀은 이 게임에 포함되지 않습니다.";
    public static final String GAME_REQUIRES_TWO_TEAMS = "게임에는 두 팀이 필요합니다.";

    public static final String PLAYER_NOT_PARTICIPANT_SCORE_EXCEPTION = "참여하지 않는 선수는 득점할 수 없습니다.";
    public static final String PLAYER_NOT_PARTICIPANT_PK_SCORE_EXCEPTION = "참여하지 않는 선수는 승부차기에서 득점할 수 없습니다.";
    public static final String PLAYER_NOT_PARTICIPANT_CANCEL_SCORE_EXCEPTION = "참여하지 않는 선수는 득점을 취소할 수 없습니다.";
    public static final String PLAYER_NOT_PARTICIPANT_ISSUE_WARNING_CARD_EXCEPTION = "참여하지 않는 선수는 경고카드를 받을 수 없습니다.";
    public static final String PLAYER_NOT_PARTICIPANT_CANCEL_WARNING_CARD_EXCEPTION = "참여하지 않는 선수는 경고카드를 취소할 수 없습니다.";
    public static final String PLAYER_FROM_ANOTHER_TEAM_REGISTER_EXCEPTION = "다른 팀의 선수를 라인업에 등록할 수 없습니다.";

}
