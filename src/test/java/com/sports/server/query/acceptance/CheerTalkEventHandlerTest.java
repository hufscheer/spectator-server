package com.sports.server.query.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.sports.server.command.cheertalk.application.CheerTalkService;
import com.sports.server.command.cheertalk.dto.CheerTalkRequest;
import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.support.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/cheer-talk-fixture.sql")
class CheerTalkEventHandlerTest extends AcceptanceTest {

    private String URL;

    private final CompletableFuture<CommentResponse> completableFuture = new CompletableFuture<>();

    @Autowired
    private CheerTalkService cheerTalkService;

    @BeforeEach
    public void setup() {
        URL = "ws://localhost:" + port + "/ws";
    }

    @Test
    void 응원톡을_작성하면_소켓_응답을_받는다() throws Exception {
        //given
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);
        StompSession stompSession = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);

        stompSession.subscribe("/topic/games/1", new CommentStompFrameHandler());

        //when
        cheerTalkService.register(new CheerTalkRequest("응원톡입니다.", 1L));

        //then
        CommentResponse actual = completableFuture.get(10, SECONDS);
        assertThat(actual.content()).isEqualTo("응원톡입니다.");
    }

    @Test
    void 응원톡의_응답_형태에_알맞은_order를_포함하고_있는지_확인한다() throws Exception {
        //given
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = messageConverter.getObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new ParameterNamesModule());
        stompClient.setMessageConverter(messageConverter);
        StompSession stompSession = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
                })
                .get(1, SECONDS);

        stompSession.subscribe("/topic/games/1", new CommentStompFrameHandler());

        //when
        cheerTalkService.register(new CheerTalkRequest("응원톡입니다.", 2L));

        //then
        CommentResponse actual = completableFuture.get(10, SECONDS);
        assertThat(actual.order()).isEqualTo(2);
    }

    private class CommentStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return CommentResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            completableFuture.complete((CommentResponse) o);
        }
    }
}