package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiSeedMessageGenerator {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final OpenRouterChatCaller chatCaller;
    private final String model;
    private final int maxLength;
    private final Set<String> bannedSoloReactions;
    private final String scheduledPrompt;
    private final String secondHalfPrompt;
    private final String goalPrompt;

    public AiSeedMessageGenerator(
            OpenRouterChatCaller chatCaller,
            @Value("${ai-seed.model:${openrouter.api.model:qwen/qwen-2.5-72b-instruct}}") String model,
            @Value("${ai-seed.max-length:15}") int maxLength,
            @Value("${ai-seed.banned-solo-reactions:ㅋㅋ,ㅋㅋㅋ,ㅋㅋㅋㅋ,ㄷㄷ,ㄷㄷㄷ,와,ㅎㅎ,ㅎㅎㅎ,ㄹㅇ}") String bannedSoloReactions,
            @Value("${ai-seed.prompt.scheduled}") String scheduledPrompt,
            @Value("${ai-seed.prompt.second-half}") String secondHalfPrompt,
            @Value("${ai-seed.prompt.goal}") String goalPrompt
    ) {
        this.chatCaller = chatCaller;
        this.model = model;
        this.maxLength = maxLength;
        this.bannedSoloReactions = Arrays.stream(bannedSoloReactions.split(","))
                .map(String::strip)
                .collect(Collectors.toSet());
        this.scheduledPrompt = scheduledPrompt;
        this.secondHalfPrompt = secondHalfPrompt;
        this.goalPrompt = goalPrompt;
    }

    public String generate(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        String prompt = buildPrompt(triggerType, teamName, scorerName);

        try {
            String result = callLlm(prompt);
            String processed = postProcess(result);
            if (processed != null) {
                return processed;
            }
        } catch (Exception e) {
            log.warn("AI Seed LLM 호출 실패, fallback 사용: {}", e.getMessage());
        }

        return fallback(triggerType, teamName, scorerName);
    }

    private String callLlm(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 30
        );

        OpenRouterChatResponse response = chatCaller.call(body, REQUEST_TIMEOUT);
        if (response == null) {
            return null;
        }
        return response.getText();
    }

    private String postProcess(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String trimmed = raw.strip();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        if (bannedSoloReactions.contains(trimmed)) {
            return null;
        }

        if (trimmed.length() > maxLength) {
            trimmed = trimmed.substring(0, maxLength);
        }

        if (trimmed.isBlank()) {
            return null;
        }

        return trimmed;
    }

    private String fallback(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        if (triggerType == AiSeedTriggerType.GOAL && scorerName != null) {
            return scorerName + " 좋았다";
        }
        return teamName + " 가자";
    }

    private String buildPrompt(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        String template = switch (triggerType) {
            case SCHEDULED -> scheduledPrompt;
            case SECOND_HALF_START -> secondHalfPrompt;
            case GOAL -> goalPrompt;
        };

        String result = template.replace("{team_name}", teamName);
        if (scorerName != null) {
            result = result.replace("{scorer_name}", scorerName);
        }
        return result;
    }
}