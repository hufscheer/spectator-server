package com.sports.server.command.timeline.domain;

/**
 * 타임라인이 바뀌었다는 사실만 알린다. 관객 화면이 점수와 기록을 다시 불러오게 하는 것이 목적이라
 * 바뀐 내용은 싣지 않는다.
 * <p>
 * {@link TimelineCreatedEvent} 와 나누어 둔 이유는 그쪽 구독자가 AI 응원 시드 생성이기 때문이다.
 * 한 이벤트로 합치면 기록을 지울 때도 시드가 돈다.
 */
public record TimelineChangedEvent(
        Long gameId,
        ChangeType changeType
) {
    public enum ChangeType {
        CREATED, DELETED
    }

    public static TimelineChangedEvent created(Long gameId) {
        return new TimelineChangedEvent(gameId, ChangeType.CREATED);
    }

    public static TimelineChangedEvent deleted(Long gameId) {
        return new TimelineChangedEvent(gameId, ChangeType.DELETED);
    }
}
