package com.sports.server.command.cheertalk.application;

/**
 * 응원톡 호출 한도와 짧은 시간 동일 본문 중복을 방어한다.
 * 구현체는 인프라 레이어에 둔다(예: Caffeine 인메모리 캐시).
 */
public interface CheerTalkRateLimiter {

    void check(String clientIp, Long gameTeamId, String content);
}
