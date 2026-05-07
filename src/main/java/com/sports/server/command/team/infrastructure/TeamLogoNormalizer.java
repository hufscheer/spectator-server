package com.sports.server.command.team.infrastructure;

import com.sports.server.command.team.domain.LogoImageNormalizationRequestedEvent;
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

    @Value("${image.origin-prefix}")
    private String originPrefix;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    public void normalize(String logoImageUrl) {
        String key = extractS3Key(logoImageUrl);
        if (key == null) {
            return;
        }
        byte[] original = s3Service.download(key);
        byte[] normalized = LogoImageNormalizer.normalize(original);
        s3Service.upload(key, normalized, LogoImageNormalizer.OUTPUT_CONTENT_TYPE);
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
