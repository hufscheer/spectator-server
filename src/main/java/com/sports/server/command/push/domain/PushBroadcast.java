package com.sports.server.command.push.domain;

import java.util.List;

public record PushBroadcast(
        PushMessage message,
        List<PushRecipient> recipients
) {
    public boolean hasRecipients() {
        return !recipients.isEmpty();
    }
}
