package com.sports.server.report.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportErrorMessage {

    public static final String NOT_FOUND_COMMENT = "존재하지 않는 댓글입니다.";
    public static final String INVALID_REPORT_BLOCKED_COMMENT = "이미 블락된 댓글은 신고할 수 없습니다.";
    public static final String REPORT_CHECK_SERVER_ERROR = "신고 검사 서버에 문제가 발생했습니다";
}
