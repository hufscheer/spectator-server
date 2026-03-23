package com.sports.server.common.infra;

import com.sports.server.common.application.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class SlackAlertClient implements AlertService {

    private final String webhookUrl;
    private final WebClient webClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SlackAlertClient(@Value("${slack.webhook.url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.webClient = WebClient.create();
    }

    @Override
    public void sendErrorAlert(String path, String method, String errorMessage, Exception exception) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String exceptionName = exception.getClass().getSimpleName();
        String text = String.format(
                ":rotating_light: *[500 ERROR]* `%s %s`\n" +
                "*Exception:* `%s`\n" +
                "*Message:* %s\n" +
                "*Time:* %s",
                method, path, exceptionName, errorMessage, timestamp
        );

        try {
            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(Map.of("text", text))
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            result -> {},
                            error -> log.warn("Slack 알림 전송 실패", error)
                    );
        } catch (Exception e) {
            log.warn("Slack 알림 전송 실패", e);
        }
    }
}
