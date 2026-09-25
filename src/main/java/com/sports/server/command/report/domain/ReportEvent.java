package com.sports.server.command.report.domain;

/**
 * 비동기로 처리하므로 엔티티 대신 id 만 담는다. 핸들러가 돌 때는 이미 영속성 컨텍스트가 닫혀 있다.
 */
public record ReportEvent(Long reportId) {
}
