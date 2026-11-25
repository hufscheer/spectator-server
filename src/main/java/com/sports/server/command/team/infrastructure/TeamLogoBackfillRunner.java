package com.sports.server.command.team.infrastructure;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.team-logo-backfill.enabled", havingValue = "true")
public class TeamLogoBackfillRunner implements ApplicationRunner {

    private final TeamRepository teamRepository;
    private final TeamLogoNormalizer teamLogoNormalizer;

    /**
     * 기본은 dry-run 이다. 켜기만 하면 S3 와 DB 에 쓰지 않고 대상 수와 크기만 로그로 남긴다.
     * 실제로 바꾸려면 {@code app.team-logo-backfill.dry-run=false} 를 함께 준다
     */
    @Value("${app.team-logo-backfill.dry-run:true}")
    private boolean dryRun;

    @Override
    public void run(ApplicationArguments args) {
        List<Team> teams = teamRepository.findAll();
        log.info("[team-logo-backfill] start. total={} dryRun={}", teams.size(), dryRun);

        int success = 0;
        int skipped = 0;
        int failed = 0;
        long originalBytes = 0;
        long normalizedBytes = 0;
        for (Team team : teams) {
            String url = team.getLogoImageUrl();
            if (url == null || url.isBlank()) {
                skipped++;
                continue;
            }
            try {
                if (dryRun) {
                    TeamLogoNormalizer.Preview preview = teamLogoNormalizer.preview(url);
                    if (preview == null) {
                        skipped++;
                        continue;
                    }
                    originalBytes += preview.originalBytes();
                    normalizedBytes += preview.normalizedBytes();
                    log.info("[team-logo-backfill] dry-run teamId={} {} -> {} ({} -> {} bytes)", team.getId(),
                            preview.key(), preview.normalizedKey(), preview.originalBytes(), preview.normalizedBytes());
                } else {
                    teamLogoNormalizer.normalize(url);
                    log.info("[team-logo-backfill] ok teamId={}", team.getId());
                }
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("[team-logo-backfill] fail teamId={} url={} reason={}",
                        team.getId(), url, e.getMessage());
            }
        }

        log.info("[team-logo-backfill] done. dryRun={} success={} skipped={} failed={} originalBytes={} normalizedBytes={}",
                dryRun, success, skipped, failed, originalBytes, normalizedBytes);
    }
}
