package com.sports.server.command.report.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportErrorMessage {

    public static final String INVALID_REPORT_BLOCKED_CHEER_TALK = "이미 블락된 응원톡은 신고할 수 없습니다.";
    public static final String REPORT_CHECK_SERVER_ERROR = "신고 검사 서버에 문제가 발생했습니다";
}
