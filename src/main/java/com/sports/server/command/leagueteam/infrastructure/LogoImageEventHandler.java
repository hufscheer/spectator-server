package com.sports.server.command.leagueteam.infrastructure;

import com.sports.server.command.leagueteam.domain.LogoImageDeletedEvent;
import com.sports.server.common.application.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LogoImageEventHandler {

    private final S3Service s3Service;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("asyncThreadPool")
    public void handle(LogoImageDeletedEvent event) {
        String keyOfImageUrl = getKeyOfImageUrl(event.logoImageUrl());
        s3Service.deleteFile(keyOfImageUrl);
    }

    private String getKeyOfImageUrl(String logoImageUrl) {
        return logoImageUrl.split(replacePrefix)[1];
    }

}
