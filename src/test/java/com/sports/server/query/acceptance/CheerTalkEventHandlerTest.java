package com.sports.server.query.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.sports.server.command.comment.application.CommentService;
import com.sports.server.command.comment.dto.CommentRequestDto;
import com.sports.server.query.dto.response.CommentResponse;
import com.sports.server.support.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@Sql(scripts = "/comment-fixture.sql")
class CheerTalkEventHandlerTest extends AcceptanceTest {

    private String URL;

    private final CompletableFuture<CommentResponse> completableFuture = new CompletableFuture<>();

    @Autowired
    private CommentService commentService;

    @BeforeEach
    public void setup() {
        URL = "ws://localhost:" + port + "/ws";
    }

    @Test
    public void testCreateGameEndpoint() throws Exception {
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
        commentService.register(new CommentRequestDto("댓글입니다.", 1L));

        //then
        CommentResponse actual = completableFuture.get(10, SECONDS);
        assertThat(actual.content()).isEqualTo("댓글입니다.");
    }

    @Test
    @DisplayName("댓글의 응답 형태에 알맞은 order 를 포함하고 있는지 확인한다")
    public void isResponseContainsExactOrder() throws Exception {
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
        commentService.register(new CommentRequestDto("댓글입니다.", 2L));

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
