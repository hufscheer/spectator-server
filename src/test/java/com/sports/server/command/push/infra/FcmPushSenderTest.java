package com.sports.server.command.push.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushPlatform;
import com.sports.server.command.push.domain.PushRecipient;
import com.sports.server.command.push.domain.PushType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class FcmPushSenderTest {

    private static final String PROJECT_ID = "hufscheer-test";
    private static final String UNREGISTERED_BODY = """
            {"error": {"code": 404, "message": "Requested entity was not found.", "status": "NOT_FOUND",
              "details": [{"@type": "type.googleapis.com/google.firebase.fcm.v1.FcmError", "errorCode": "UNREGISTERED"}]}}
            """;
    private static final String INVALID_TOKEN_BODY = """
            {"error": {"code": 400, "message": "The registration token is not a valid FCM registration token", "status": "INVALID_ARGUMENT"}}
            """;
    private static final String INVALID_PAYLOAD_BODY = """
            {"error": {"code": 400, "message": "Invalid value at 'message.data'", "status": "INVALID_ARGUMENT"}}
            """;

    private final PushMessage message = PushMessage.of(
            PushType.GAME_START, 7L, "/org/1/soccer/games/7", "경기 시작", "팀A vs 팀B");
    private final PushRecipient recipient = new PushRecipient(11L, "token-11", PushPlatform.ANDROID, "abcd1234");

    @Nested
    @DisplayName("FCM 응답에 따라")
    class Response {

        @Test
        void 성공하면_지울_기기가_없다() {
            // given
            RecordedExchange exchange = new RecordedExchange(request -> ok());

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).isEmpty();
            assertThat(exchange.requests).hasSize(1);
            ClientRequest request = exchange.requests.get(0);
            assertThat(request.url().toString())
                    .isEqualTo("https://fcm.googleapis.com/v1/projects/hufscheer-test/messages:send");
            assertThat(request.headers().getFirst("Authorization")).isEqualTo("Bearer access-token");
        }

        @Test
        void UNREGISTERED_토큰의_기기는_지울_대상이다() {
            // given
            RecordedExchange exchange = new RecordedExchange(request -> failure(HttpStatus.NOT_FOUND, UNREGISTERED_BODY));

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).containsExactly(11L);
        }

        @Test
        void 토큰이_잘못됐다는_INVALID_ARGUMENT는_지울_대상이다() {
            // given
            RecordedExchange exchange = new RecordedExchange(request -> failure(HttpStatus.BAD_REQUEST, INVALID_TOKEN_BODY));

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).containsExactly(11L);
        }

        @Test
        void 페이로드가_잘못됐다는_INVALID_ARGUMENT는_기기를_지우지_않고_재시도도_하지_않는다() {
            // given
            RecordedExchange exchange = new RecordedExchange(request -> failure(HttpStatus.BAD_REQUEST, INVALID_PAYLOAD_BODY));

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).isEmpty();
            assertThat(exchange.requests).hasSize(1);
        }

        @Test
        void 서버_오류는_다시_시도해_성공하면_끝난다() {
            // given
            AtomicInteger calls = new AtomicInteger();
            RecordedExchange exchange = new RecordedExchange(request ->
                    calls.incrementAndGet() == 1 ? failure(HttpStatus.SERVICE_UNAVAILABLE, "{}") : ok());

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).isEmpty();
            assertThat(exchange.requests).hasSize(2);
        }

        @Test
        void 계속_429면_제한_횟수에서_멈추고_기기는_지우지_않는다() {
            // given
            RecordedExchange exchange = new RecordedExchange(request -> failure(HttpStatus.TOO_MANY_REQUESTS, "{}"));

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient));

            // then
            assertThat(invalid).isEmpty();
            assertThat(exchange.requests).hasSize(3);
        }

        @Test
        void 여러_기기에_각각_보내고_무효한_기기만_모은다() {
            // given
            PushRecipient other = new PushRecipient(12L, "token-12", PushPlatform.IOS, "efgh5678");
            RecordedExchange exchange = new RecordedExchange(request -> failure(HttpStatus.NOT_FOUND, UNREGISTERED_BODY));

            // when
            List<Long> invalid = sender(exchange).send(message, List.of(recipient, other));

            // then
            assertThat(exchange.requests).hasSize(2);
            assertThat(invalid).containsExactlyInAnyOrder(11L, 12L);
        }
    }

    @Nested
    @DisplayName("전송 시간이 다 되면")
    class BroadcastTimeout {

        @Test
        void 거기까지_모은_무효_기기를_돌려준다() {
            // given
            PushRecipient slow = new PushRecipient(12L, "token-12", PushPlatform.IOS, "efgh5678");
            // 첫 기기는 무효로 답하고, 둘째 기기는 끝내 답하지 않는다
            AtomicInteger calls = new AtomicInteger();
            RecordedExchange exchange = new RecordedExchange(request ->
                    calls.incrementAndGet() == 1 ? failure(HttpStatus.NOT_FOUND, UNREGISTERED_BODY) : Mono.never());
            FcmPushSender sender = sender(exchange, Duration.ofMillis(500));

            // when
            List<Long> invalid = sender.send(message, List.of(recipient, slow));

            // then
            assertThat(invalid).containsExactly(11L);
        }
    }

    @Nested
    @DisplayName("요청 본문은")
    class Body {

        @Test
        void 안드로이드는_우선순위를_높인다() {
            Map<String, Object> body = messageOf(PushPlatform.ANDROID, null);

            assertThat(body).containsKeys("token", "notification", "data", "android")
                    .doesNotContainKeys("apns", "webpush");
            assertThat(body.get("notification")).isEqualTo(Map.of("title", "경기 시작", "body", "팀A vs 팀B"));
        }

        @Test
        void iOS는_APNs_블록을_붙인다() {
            Map<String, Object> body = messageOf(PushPlatform.IOS, null);

            assertThat(body).containsKey("apns").doesNotContainKeys("android", "webpush");
        }

        @Test
        void 웹은_절대_주소_링크를_붙인다() {
            Map<String, Object> body = messageOf(PushPlatform.WEB, "https://hufscheer.com/");

            @SuppressWarnings("unchecked")
            Map<String, Object> webpush = (Map<String, Object>) body.get("webpush");
            assertThat(webpush.get("fcm_options"))
                    .isEqualTo(Map.of("link", "https://hufscheer.com/org/1/soccer/games/7"));
        }

        @Test
        void 웹_주소_설정이_없으면_링크를_붙이지_않는다() {
            Map<String, Object> body = messageOf(PushPlatform.WEB, null);

            @SuppressWarnings("unchecked")
            Map<String, Object> webpush = (Map<String, Object>) body.get("webpush");
            assertThat(webpush).doesNotContainKey("fcm_options");
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> messageOf(PushPlatform platform, String webBaseUrl) {
            PushRecipient target = new PushRecipient(1L, "token", platform, "abcd1234");
            return (Map<String, Object>) FcmMessageBody.of(target, message, webBaseUrl).get("message");
        }
    }

    private FcmPushSender sender(RecordedExchange exchange) {
        return sender(exchange, null);
    }

    private FcmPushSender sender(RecordedExchange exchange, Duration broadcastTimeout) {
        WebClient webClient = WebClient.builder().exchangeFunction(exchange).build();
        FcmProperties properties = new FcmProperties(true, PROJECT_ID, "{}", null);
        if (broadcastTimeout == null) {
            return new FcmPushSender(webClient, properties, () -> "access-token");
        }
        return new FcmPushSender(webClient, properties, () -> "access-token", broadcastTimeout);
    }

    private static Mono<ClientResponse> ok() {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\": \"projects/hufscheer-test/messages/1\"}")
                .build());
    }

    private static Mono<ClientResponse> failure(HttpStatus status, String body) {
        return Mono.just(ClientResponse.create(status)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
    }

    private static class RecordedExchange implements org.springframework.web.reactive.function.client.ExchangeFunction {

        private final List<ClientRequest> requests = new ArrayList<>();
        private final Function<ClientRequest, Mono<ClientResponse>> responder;

        RecordedExchange(Function<ClientRequest, Mono<ClientResponse>> responder) {
            this.responder = responder;
        }

        @Override
        public synchronized Mono<ClientResponse> exchange(ClientRequest request) {
            requests.add(request);
            return responder.apply(request);
        }
    }
}
