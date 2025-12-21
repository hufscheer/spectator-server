package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.sports.server.command.cheertalk.domain.BotType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class BeomiClient implements CheerTalkBotClient {

    private final WebClient beomiWebClient;

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
                .uri("/users/beomi/apps/text-moderation/models/moderation-abuse-korean/versions/f6fb536be02f4c34a92be44c1093ce55/outputs")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}