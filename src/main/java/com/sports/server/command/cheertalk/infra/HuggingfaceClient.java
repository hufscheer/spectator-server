package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class HuggingfaceClient implements CheerTalkBotClient {

    private final WebClient huggingfaceWebClient;

    @Override
    public BotType supports() {
        return BotType.KOR_UNSMILE;
    }

    @Override
    public JsonNode detectAbusiveContent(String content) {
        return huggingfaceWebClient.post()
                .uri("/smilegate-ai/kor_unsmile")
                .bodyValue(Map.of("inputs", content))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}