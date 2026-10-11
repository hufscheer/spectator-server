package com.sports.server.command.push.infra;

import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushPlatform;
import com.sports.server.command.push.domain.PushRecipient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FCM HTTP v1 {@code messages:send} 요청 본문. 플랫폼마다 필요한 블록만 붙인다.
 */
class FcmMessageBody {

    private static final String TTL_SECONDS = "3600";

    private FcmMessageBody() {
    }

    static Map<String, Object> of(PushRecipient recipient, PushMessage message, String webBaseUrl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", recipient.token());
        body.put("notification", Map.of("title", message.title(), "body", message.body()));
        body.put("data", message.data());
        body.putAll(platformBlock(recipient.platform(), message, webBaseUrl));
        return Map.of("message", body);
    }

    private static Map<String, Object> platformBlock(PushPlatform platform, PushMessage message, String webBaseUrl) {
        return switch (platform) {
            case ANDROID -> Map.of("android", Map.of("priority", "HIGH", "ttl", TTL_SECONDS + "s"));
            case IOS -> Map.of("apns", Map.of(
                    "headers", Map.of("apns-priority", "10"),
                    "payload", Map.of("aps", Map.of("sound", "default"))
            ));
            case WEB -> Map.of("webpush", webpush(message, webBaseUrl));
        };
    }

    private static Map<String, Object> webpush(PushMessage message, String webBaseUrl) {
        Map<String, Object> webpush = new LinkedHashMap<>();
        webpush.put("headers", Map.of("TTL", TTL_SECONDS, "Urgency", "high"));
        if (webBaseUrl != null && !webBaseUrl.isBlank()) {
            webpush.put("fcm_options", Map.of("link", absoluteUrl(webBaseUrl, message.url())));
        }
        return webpush;
    }

    private static String absoluteUrl(String webBaseUrl, String path) {
        String base = webBaseUrl.endsWith("/") ? webBaseUrl.substring(0, webBaseUrl.length() - 1) : webBaseUrl;
        return base + path;
    }
}
