package com.sports.server.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String DESTINATION_PREFIX = "/topic";

    // Cloudflare 가 데이터가 오가지 않는 WebSocket 을 2분쯤 뒤 끊는다. 10초마다 하트비트를 주고받아 연결을 유지한다.
    private static final long[] HEARTBEAT_MILLIS = {10_000, 10_000};

    private final TaskScheduler messageBrokerTaskScheduler;

    // @EnableScheduling 의 taskScheduler 와 겹치므로 STOMP 브로커 전용 스케줄러를 이름으로 받는다. @Lazy 는 순환 참조를 피한다.
    public WebSocketConfig(@Lazy @Qualifier("messageBrokerTaskScheduler") TaskScheduler messageBrokerTaskScheduler) {
        this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(DESTINATION_PREFIX)
                .setHeartbeatValue(HEARTBEAT_MILLIS)
                .setTaskScheduler(messageBrokerTaskScheduler);
    }

    // /topic 은 서버가 저장과 검사를 마친 뒤에만 방송한다. 클라이언트 SEND 를 받으면 브로커가 그대로 구독자에게 뿌린다.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.SEND.equals(accessor.getCommand())) {
                    throw new MessageDeliveryException(message, "클라이언트는 메시지를 보낼 수 없습니다.");
                }
                return message;
            }
        });
    }

}
