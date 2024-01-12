package com.sports.server.command.report.infrastructure;

public record ReportCheckRequest(
        String target,
        Long cheerTalkId,
        Long reportId
) {
}
