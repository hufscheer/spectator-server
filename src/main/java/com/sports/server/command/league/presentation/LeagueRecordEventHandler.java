package com.sports.server.command.league.presentation;

import com.sports.server.command.league.application.LeagueCheerRankingService;
import com.sports.server.command.league.application.LeagueTopScorerService;
import com.sports.server.command.league.domain.LeagueRecordRecalculationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 득점왕과 응원왕, 댓글왕은 경기 종료가 커밋된 뒤 다시 센다. 종료 트랜잭션 안에서 세면 그 트랜잭션의 스냅샷이라
 * 같은 대회에서 동시에 끝난 다른 경기를 놓친다. 트랜잭션 밖(자정 자동 종료)에서 오면 바로 센다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeagueRecordEventHandler {

    private final LeagueTopScorerService leagueTopScorerService;
    private final LeagueCheerRankingService leagueCheerRankingService;

    // 경기 종료는 이미 커밋됐으니, 다시 세다 실패해도 요청을 실패로 돌리지 않고 남긴다.
    // 하나가 실패해도 다른 집계는 돈다
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(LeagueRecordRecalculationEvent event) {
        try {
            leagueTopScorerService.updateTopScorersForLeague(event.leagueId());
        } catch (Exception e) {
            log.error("득점왕 재집계 실패: leagueId={}, error={}", event.leagueId(), e.getMessage(), e);
        }
        try {
            leagueCheerRankingService.updateCheerRankingsForLeague(event.leagueId());
        } catch (Exception e) {
            log.error("응원 순위 재집계 실패: leagueId={}, error={}", event.leagueId(), e.getMessage(), e);
        }
    }
}
