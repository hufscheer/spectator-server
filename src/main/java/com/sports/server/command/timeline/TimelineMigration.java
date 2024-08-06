package com.sports.server.command.timeline;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.record.domain.Record;
import com.sports.server.command.record.domain.RecordType;
import com.sports.server.command.record.domain.ReplacementRecord;
import com.sports.server.command.record.domain.ScoreRecord;
import com.sports.server.command.timeline.domain.ReplacementTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;


/**
 * TODO 마이그레이션 완료 이후 Record 테이블과 함께 삭제 예정
 */
@Component
@RequiredArgsConstructor
public class TimelineMigration {
    private final TimelineRepository timelineRepository;
    private final EntityManager em;
    private final TransactionTemplate transactionTemplate;

    @PostConstruct
    void migrateTimeline() {
        transactionTemplate.executeWithoutResult(status -> {
            // 마이그레이션 이후 이 클래스를 삭제하지 않고 배포 시 다시 마이그레이션 되지 않도록 함
            if (alreadyMigrated()) return;

            List<Long> gameIds = getGameIds();

            gameIds.forEach(this::migrateTimeline);
        });
    }

    private boolean alreadyMigrated() {
        return em.createQuery("SELECT COUNT(t) > 0 FROM Timeline t", Boolean.class)
                .getSingleResult();
    }

    private List<Long> getGameIds() {
        return em.createQuery("SELECT g FROM Game g", Game.class)
                .getResultList()
                .stream().map(Game::getId)
                .toList();
    }

    private void migrateTimeline(Long gameId) {
        List<Record> records = em.createQuery(
                "SELECT r FROM Record r WHERE r.game.id = :gameId ORDER BY r.id",
                        Record.class)
                .setParameter("gameId", gameId)
                .getResultList();

        // 올바른 스냅샷 점수 생성을 위해 Game Score를 0:0으로 초기화
        clearGameScore(records);

        for (Record record : records) {
            timelineRepository.save(convert(record));
        }
    }

    private Timeline convert(Record record) {
        if (record.getRecordType() == RecordType.SCORE) {
            ScoreRecord scoreRecord = getRecord(record, ScoreRecord.class);

            return ScoreTimeline.score(
                    record.getGame(),
                    record.getRecordedQuarter(),
                    record.getRecordedAt(),
                    scoreRecord.getLineupPlayer()
            );
        } else if (record.getRecordType() == RecordType.REPLACEMENT) {
            ReplacementRecord replacementRecord = getRecord(record, ReplacementRecord.class);

            return new ReplacementTimeline(
                    record.getGame(),
                    record.getRecordedQuarter(),
                    record.getRecordedAt(),
                    replacementRecord.getOriginLineupPlayer(),
                    replacementRecord.getReplacedLineupPlayer()
            );
        }

        return null;
    }

    private <E> E getRecord(Record record, Class<E> clazz) {
        return em.createQuery("SELECT r FROM " + clazz.getSimpleName() + " r WHERE r.record.id = :recordId", clazz)
                .setParameter("recordId", record.getId())
                .getSingleResult();
    }

    private void clearGameScore(List<Record> records) {
        if (!records.isEmpty()) {
            Game game = records.get(0).getGame();

            clearScore(game.getTeam1());
            clearScore(game.getTeam2());
        }
    }

    private void clearScore(GameTeam gameTeam) {
        while (gameTeam.getScore() > 0) {
            gameTeam.cancelScore();
        }
    }
}
