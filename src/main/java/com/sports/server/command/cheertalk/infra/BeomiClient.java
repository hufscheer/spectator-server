package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class BeomiClient implements CheerTalkBotClient {

    private final WebClient beomiWebClient;

    @Value("${beomi.uri}")
    private String beomiUri;

    @Override
    public BotType supports() {
        return BotType.BEOMI;
    }

    @Override
    public JsonNode detectAbusiveContent(String content) {
        Map<String, Object> requestBody = Map.of(
                "inputs", List.of(
                        Map.of(
                                "data", Map.of(
                                        "text", Map.of(
                                                "raw", content
                                        )
                                )
                        )
                )
        );

        return beomiWebClient.post()
                .uri(beomiUri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}