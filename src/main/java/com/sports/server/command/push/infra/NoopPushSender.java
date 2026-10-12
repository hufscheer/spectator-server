package com.sports.server.command.push.infra;

import com.sports.server.command.push.application.PushSender;
import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushRecipient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopPushSender implements PushSender {

    @Override
    public List<Long> send(PushMessage message, List<PushRecipient> recipients) {
        log.info("FCM 비활성 상태라 푸시를 보내지 않았습니다: type={}, 수신자={}명, title={}",
                message.data().get(PushMessage.TYPE_KEY), recipients.size(), message.title());
        return List.of();
    }
}
