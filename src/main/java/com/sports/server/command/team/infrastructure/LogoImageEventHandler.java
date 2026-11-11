package com.sports.server.command.team.infrastructure;

import com.sports.server.command.team.domain.LogoImageDeletedEvent;
import com.sports.server.command.team.domain.TeamRepository;
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
    private final TeamRepository teamRepository;

    @Value("${image.replaced-prefix}")
    private String replacePrefix;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("asyncThreadPool")
    public void handle(LogoImageDeletedEvent event) {
        String logoImageUrl = event.logoImageUrl();
        if (logoImageUrl == null || !logoImageUrl.startsWith(replacePrefix)) {
            return;
        }
        // 다른 팀이 같은 로고 주소를 아직 쓰면 그 팀의 로고가 사라지므로 지우지 않는다
        if (teamRepository.countByLogoImageUrl(logoImageUrl) > 0) {
            return;
        }
        s3Service.deleteFile(getKeyOfImageUrl(logoImageUrl));
    }

    private String getKeyOfImageUrl(String logoImageUrl) {
        return logoImageUrl.substring(replacePrefix.length());
    }

}
