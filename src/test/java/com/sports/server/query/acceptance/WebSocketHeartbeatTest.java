package com.sports.server.query.acceptance;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.support.AcceptanceTest;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * Cloudflare 는 데이터가 오가지 않는 WebSocket 을 2분쯤 뒤 끊는다(2026-10-06 실측 126초).
 * 서버가 STOMP 하트비트를 주고받아야 조용한 경기 중에도 연결이 유지된다.
 */
class WebSocketHeartbeatTest extends AcceptanceTest {

    private final ThreadPoolTaskScheduler clientScheduler = new ThreadPoolTaskScheduler();

    @AfterEach
    void tearDown() {
        clientScheduler.shutdown();
    }

    @Test
    void 연결하면_서버가_하트비트를_주고받기로_응답한다() throws Exception {
        // given: 프론트(@stomp/stompjs 기본값)처럼 10초 하트비트를 요청한다
        clientScheduler.initialize();
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setTaskScheduler(clientScheduler);
        stompClient.setDefaultHeartbeat(new long[]{10_000, 10_000});

        CompletableFuture<long[]> serverHeartbeat = new CompletableFuture<>();

        // when
        StompSession session = stompClient.connectAsync("ws://localhost:" + port + "/ws",
                        new StompSessionHandlerAdapter() {
                            @Override
                            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                                serverHeartbeat.complete(connectedHeaders.getHeartbeat());
                            }
                        })
                .get(5, SECONDS);

        // then: CONNECTED 프레임의 heart-beat 가 0,0 이 아니다
        assertThat(serverHeartbeat.get(5, SECONDS)).containsExactly(10_000, 10_000);
        session.disconnect();
    }
}
