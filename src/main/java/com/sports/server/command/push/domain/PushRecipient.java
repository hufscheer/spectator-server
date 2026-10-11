package com.sports.server.command.push.domain;

public record PushRecipient(
        Long deviceId,
        String token,
        PushPlatform platform,
        String fingerprint
) {
    public static PushRecipient from(PushDevice device) {
        return new PushRecipient(device.getId(), device.getToken(), device.getPlatform(), device.fingerprint());
    }
}
