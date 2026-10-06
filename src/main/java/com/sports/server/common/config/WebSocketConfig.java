package com.sports.server.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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

}
