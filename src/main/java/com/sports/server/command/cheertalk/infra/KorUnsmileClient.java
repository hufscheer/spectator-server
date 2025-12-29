package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KorUnsmileClient implements CheerTalkBotClient {

    private final WebClient korUnsmileWebClient;

    @Value("${korunsmile.uri}")
    private String korUnsmileUri;

    @Override
    public BotType supports() {
        return BotType.KOR_UNSMILE;
    }

    @Override
    public JsonNode detectAbusiveContent(String content) {
        return korUnsmileWebClient.post()
                .uri(korUnsmileUri)
                .bodyValue(Map.of("inputs", content))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}