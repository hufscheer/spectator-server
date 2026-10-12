package com.sports.server.command.push.application;

import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushRecipient;
import java.util.List;

public interface PushSender {

    /**
     * @return 더는 쓸 수 없는 토큰이라 지워야 하는 기기 id
     */
    List<Long> send(PushMessage message, List<PushRecipient> recipients);
}
