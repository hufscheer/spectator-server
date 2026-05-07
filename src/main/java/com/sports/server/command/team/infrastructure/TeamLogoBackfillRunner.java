package com.sports.server.command.team.infrastructure;

import com.sports.server.command.team.domain.Team;
import com.sports.server.command.team.domain.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(ApplicationArguments args) {
        List<Team> teams = teamRepository.findAll();
        log.info("[team-logo-backfill] start. total={}", teams.size());

        int success = 0;
        int skipped = 0;
        int failed = 0;
        for (Team team : teams) {
            String url = team.getLogoImageUrl();
            if (url == null || url.isBlank()) {
                skipped++;
                continue;
            }
            try {
                teamLogoNormalizer.normalize(url);
                success++;
                log.info("[team-logo-backfill] ok teamId={}", team.getId());
            } catch (Exception e) {
                failed++;
                log.warn("[team-logo-backfill] fail teamId={} url={} reason={}",
                        team.getId(), url, e.getMessage());
            }
        }

        log.info("[team-logo-backfill] done. success={} skipped={} failed={}", success, skipped, failed);
    }
}
