package com.sports.server.report.infrastructure;

public record ReportCheckRequest(
        String target,
        Long commentId,
        Long reportId
) {
}
