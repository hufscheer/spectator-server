package com.sports.server.command.push.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sports.server.command.push.application.PushSender;
import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushRecipient;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
public class FcmPushSender implements PushSender {

    private static final String SEND_URL = "https://fcm.googleapis.com/v1/projects/{projectId}/messages:send";
    private static final int CONCURRENCY = 4;
    private static final int MAX_RETRIES = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(500);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration BROADCAST_TIMEOUT = Duration.ofMinutes(2);
    private static final String UNREGISTERED = "UNREGISTERED";
    private static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    private static final String INVALID_TOKEN_MESSAGE = "registration token";

    private final WebClient webClient;
    private final FcmProperties properties;
    private final FcmAccessTokenProvider accessTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FcmPushSender(WebClient webClient, FcmProperties properties, FcmAccessTokenProvider accessTokenProvider) {
        this.webClient = webClient;
        this.properties = properties;
        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public List<Long> send(PushMessage message, List<PushRecipient> recipients) {
        String accessToken = accessTokenProvider.accessToken();

        List<Long> invalidDeviceIds = Flux.fromIterable(recipients)
                .flatMap(recipient -> sendTo(recipient, message, accessToken), CONCURRENCY)
                .collectList()
                .block(BROADCAST_TIMEOUT);
        return invalidDeviceIds == null ? List.of() : invalidDeviceIds;
    }

    // 토큰이 무효하면 그 기기 id 를, 아니면 빈 Mono 를 낸다
    private Mono<Long> sendTo(PushRecipient recipient, PushMessage message, String accessToken) {
        return webClient.post()
                .uri(SEND_URL, properties.getProjectId())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FcmMessageBody.of(recipient, message, properties.getWebBaseUrl()))
                .exchangeToMono(response -> handle(response, recipient))
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF).filter(FcmPushSender::isRetryable))
                .onErrorResume(error -> {
                    log.warn("FCM 전송 실패: device={}, error={}", recipient.fingerprint(), error.toString());
                    return Mono.empty();
                });
    }

    private Mono<Long> handle(ClientResponse response, PushRecipient recipient) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.releaseBody().then(Mono.empty());
        }
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> handleFailure(response.statusCode().value(), body, recipient));
    }

    private Mono<Long> handleFailure(int status, String body, PushRecipient recipient) {
        if (isInvalidToken(body)) {
            log.info("FCM 토큰이 무효해 기기를 지웁니다: device={}", recipient.fingerprint());
            return Mono.just(recipient.deviceId());
        }
        if (isRetryableStatus(status)) {
            return Mono.error(new RetryableSendException(status));
        }
        log.warn("FCM 전송 거절: device={}, status={}, error={}", recipient.fingerprint(), status, errorMessageOf(body));
        return Mono.empty();
    }

    // INVALID_ARGUMENT 는 페이로드 문제일 때도 나오므로 메시지가 토큰을 가리킬 때만 무효로 본다
    private boolean isInvalidToken(String body) {
        JsonNode error = parse(body).path("error");
        String errorCode = errorCodeOf(error);
        if (UNREGISTERED.equals(errorCode)) {
            return true;
        }
        return INVALID_ARGUMENT.equals(error.path("status").asText())
                && error.path("message").asText().contains(INVALID_TOKEN_MESSAGE);
    }

    private String errorCodeOf(JsonNode error) {
        for (JsonNode detail : error.path("details")) {
            if (detail.hasNonNull("errorCode")) {
                return detail.get("errorCode").asText();
            }
        }
        return error.path("status").asText();
    }

    private String errorMessageOf(String body) {
        return parse(body).path("error").path("message").asText(body);
    }

    private JsonNode parse(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            return objectMapper.nullNode();
        }
    }

    private static boolean isRetryable(Throwable error) {
        return error instanceof RetryableSendException
                || error instanceof WebClientRequestException
                || error instanceof java.util.concurrent.TimeoutException;
    }

    private static boolean isRetryableStatus(int status) {
        return status == HttpStatus.TOO_MANY_REQUESTS.value() || status >= HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private static class RetryableSendException extends RuntimeException {
        RetryableSendException(int status) {
            super("FCM status " + status);
        }
    }
}
