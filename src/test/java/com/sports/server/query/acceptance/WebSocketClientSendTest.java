package com.sports.server.query.acceptance;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.support.AcceptanceTest;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * /topic 은 서버만 방송한다. 익명 클라이언트가 SEND 로 관중 화면에 가짜 응원톡이나 타임라인을 뿌리면 안 된다.
 */
class WebSocketClientSendTest extends AcceptanceTest {

    private static final String GAME_TOPIC = "/topic/games/1";

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketStompClient stompClient;
    private StompSession viewer;
    private final BlockingQueue<String> received = new LinkedBlockingQueue<>();

    @BeforeEach
    void connectViewer() throws Exception {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new StringMessageConverter());
        viewer = connect(new StompSessionHandlerAdapter() {
        });
        viewer.subscribe(GAME_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((String) payload);
            }
        });
        // 구독은 비동기로 등록된다. 서버 방송이 닿을 때까지 다시 보내 등록을 확인한다
        String ready = null;
        for (int i = 0; i < 50 && ready == null; i++) {
            messagingTemplate.convertAndSend(GAME_TOPIC, "ready");
            ready = received.poll(100, MILLISECONDS);
        }
        assertThat(ready).contains("ready");
        Thread.sleep(300);
        received.clear();
    }

    @AfterEach
    void tearDown() {
        if (viewer != null && viewer.isConnected()) {
            viewer.disconnect();
        }
    }

    @Test
    void 익명_클라이언트의_SEND_는_구독자에게_전달되지_않는다() throws Exception {
        // given
        CompletableFuture<String> attackerError = new CompletableFuture<>();
        StompSession attacker = connect(new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                attackerError.complete(headers.getFirst("message"));
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
        });

        // when
        attacker.send(GAME_TOPIC, "{\"content\":\"spoof\"}");
        attacker.send(GAME_TOPIC + "/timeline", "{\"changeType\":\"DELETED\"}");

        // then: 관중은 아무것도 받지 않고, 공격자는 ERROR 프레임을 받는다
        assertThat(received.poll(2, SECONDS)).isNull();
        assertThat(attackerError.get(5, SECONDS)).isNotNull();
    }

    @Test
    void 서버_방송은_구독자에게_계속_전달된다() throws Exception {
        // when
        messagingTemplate.convertAndSend(GAME_TOPIC, "server");

        // then
        assertThat(received.poll(5, SECONDS)).contains("server");
    }

    private StompSession connect(StompSessionHandlerAdapter handler) throws Exception {
        return stompClient.connectAsync("ws://localhost:" + port + "/ws", handler).get(5, SECONDS);
    }
}
