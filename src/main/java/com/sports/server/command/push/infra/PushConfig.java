package com.sports.server.command.push.infra;

import com.google.auth.oauth2.GoogleCredentials;
import com.sports.server.command.push.application.PushSender;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(FcmProperties.class)
public class PushConfig {

    private static final List<String> FCM_SCOPES = List.of("https://www.googleapis.com/auth/firebase.messaging");

    @Bean
    public PushSender pushSender(FcmProperties properties) {
        if (!properties.isConfigured()) {
            log.info("FCM 설정이 없어 푸시 전송을 끕니다 (enabled={})", properties.isEnabled());
            return new NoopPushSender();
        }
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(properties.getCredentialsJson().getBytes(StandardCharsets.UTF_8)))
                    .createScoped(FCM_SCOPES);
            return new FcmPushSender(WebClient.create(), properties, new GoogleFcmAccessTokenProvider(credentials));
        } catch (IOException e) {
            log.error("FCM 서비스 계정 JSON 을 읽지 못해 푸시 전송을 끕니다", e);
            return new NoopPushSender();
        }
    }
}
