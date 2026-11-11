package com.sports.server.command.team.infrastructure;

import com.sports.server.command.team.domain.LogoImageNormalizationRequestedEvent;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.common.application.S3Service;
import com.sports.server.common.util.LogoImageNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamLogoNormalizer {

    private final S3Service s3Service;
    private final TeamRepository teamRepository;

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    /**
     * 원본을 512px WebP 로 바꿔 새 키({@code normalized/...})에 올리고, 원본을 가리키던 팀 로고 주소를 새 주소로 바꾼 뒤
     * 원본은 로고 삭제와 같은 방식({@code backup/} 으로 복사 후 삭제)으로 치운다.
     *
     * <p>어느 단계에서 실패하든 DB 를 바꾸기 전이면 팀은 원본 주소를 그대로 쓴다. 원본 키를 덮어쓰지 않는 까닭은
     * {@link LogoImageNormalizer#NORMALIZED_PREFIX} 에 있다.
     */
    public void normalize(String logoImageUrl) {
        String key = extractS3Key(logoImageUrl);
        if (key == null || LogoImageNormalizer.isNormalizedKey(key)) {
            return;
        }
        byte[] original = s3Service.download(key);
        byte[] normalized = LogoImageNormalizer.normalize(original);
        String normalizedKey = LogoImageNormalizer.normalizedKeyOf(key);
        s3Service.upload(normalizedKey, normalized, LogoImageNormalizer.OUTPUT_CONTENT_TYPE);

        int updatedTeams = teamRepository.replaceLogoImageUrl(replacePrefix + key, replacePrefix + normalizedKey);
        if (updatedTeams == 0) {
            // 그 사이 다른 로고로 바뀌었다. 원본도 정규화본도 아무 팀이 가리키지 않으니 그대로 둔다
            log.info("[team-logo-normalize] no team uses {} anymore, skip replacing", key);
            return;
        }
        log.info("[team-logo-normalize] {} -> {} ({} -> {} bytes, teams={})",
                key, normalizedKey, original.length, normalized.length, updatedTeams);
        deleteOriginal(key);
    }

    /**
     * 쓰지 않고 결과 크기만 본다(백필 dry-run). 정규화 대상이 아니면 null.
     */
    public Preview preview(String logoImageUrl) {
        String key = extractS3Key(logoImageUrl);
        if (key == null || LogoImageNormalizer.isNormalizedKey(key)) {
            return null;
        }
        byte[] original = s3Service.download(key);
        byte[] normalized = LogoImageNormalizer.normalize(original);
        return new Preview(key, LogoImageNormalizer.normalizedKeyOf(key), original.length, normalized.length);
    }

    public record Preview(String key, String normalizedKey, int originalBytes, int normalizedBytes) {
    }

    private void deleteOriginal(String key) {
        try {
            s3Service.deleteFile(key);
        } catch (Exception e) {
            // 팀은 이미 새 주소를 쓴다. 원본이 남는 것뿐이라 실패로 보지 않는다
            log.warn("[team-logo-normalize] original not deleted key={} reason={}", key, e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("asyncThreadPool")
    public void handle(LogoImageNormalizationRequestedEvent event) {
        try {
            normalize(event.logoImageUrl());
        } catch (Exception e) {
            log.warn("[team-logo-normalize-async] failed url={} reason={}",
                    event.logoImageUrl(), e.getMessage());
        }
    }

    private String extractS3Key(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (url.startsWith(originPrefix)) {
            return url.substring(originPrefix.length());
        }
        if (url.startsWith(replacePrefix)) {
            return url.substring(replacePrefix.length());
        }
        return null;
    }
}
