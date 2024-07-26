package com.sports.server.command.leagueteam.infrastructure;

import com.sports.server.command.leagueteam.infrastructure.LogoImageDeletedEvent;
import com.sports.server.common.application.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LogoImageEventHandler {

    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("asyncThreadPool")
    public void handle(LogoImageDeletedEvent event) {
        String keyOfImageUrl = event.keyOfImageUrl();
        s3Service.deleteFile(keyOfImageUrl);
    }

}
