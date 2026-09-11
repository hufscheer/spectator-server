package com.sports.server.command.cheertalk.infra;

import com.sports.server.command.cheertalk.domain.AiSeedTriggerType;
import com.sports.server.common.infra.openrouter.OpenRouterChatCaller;
import com.sports.server.common.infra.openrouter.OpenRouterChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai-seed.provider", havingValue = "openrouter")
public class AiSeedMessageGenerator {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    /** 프롬프트가 "앞에 기호를 붙이지 마라" 라고 해도 모델이 붙일 때가 있다 */
    private static final Pattern LEADING_BULLET = Pattern.compile("^[-*\\u2022]\\s*|^\\d+[.)]\\s*");

    /**
     * 팀 이름이 이보다 길면 프롬프트에서 팀명을 쓰지 말라고 못박는다.
     * "서울시립대학교 남자축구 대표팀"(15자)을 문장에 넣으면 30자 예산의 절반이 날아가고,
     * 사람은 애초에 팀 이름을 저렇게 통째로 치지 않는다 — "경희대", "똘리" 라고 쓴다.
     */
    private static final int TEAM_NAME_TOO_LONG = 8;

    /**
     * 한글은 토큰당 글자 수가 적어 30 토큰이면 문장이 끝나기 전에 잘린다.
     * 넉넉히 받고 긴 것은 postProcess 에서 버린다.
     */
    private static final int MAX_TOKENS = 80;

    /** 다듬어서 버려졌을 때 한 번 더 물어본다. 출력이 매번 달라 두 번째는 대개 통과한다 */
    private static final int LLM_ATTEMPTS = 2;

    private final OpenRouterChatCaller chatCaller;
    private final String model;
    private final int maxLength;
    private final Set<String> bannedSoloReactions;
    private final String scheduledPrompt;
    private final String secondHalfPrompt;
    private final String goalPrompt;
    private final String ownGoalPrompt;

    public AiSeedMessageGenerator(
            OpenRouterChatCaller chatCaller,
            @Value("${ai-seed.model:${openrouter.api.model:qwen/qwen-2.5-72b-instruct}}") String model,
            @Value("${ai-seed.max-length:15}") int maxLength,
            @Value("${ai-seed.banned-solo-reactions:ㅋㅋ,ㅋㅋㅋ,ㅋㅋㅋㅋ,ㄷㄷ,ㄷㄷㄷ,와,ㅎㅎ,ㅎㅎㅎ,ㄹㅇ}") String bannedSoloReactions,
            @Value("${ai-seed.prompt.scheduled}") String scheduledPrompt,
            @Value("${ai-seed.prompt.second-half}") String secondHalfPrompt,
            @Value("${ai-seed.prompt.goal}") String goalPrompt,
            @Value("${ai-seed.prompt.own-goal}") String ownGoalPrompt
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
        this.ownGoalPrompt = ownGoalPrompt;
    }

    /**
     * 모델이 쓸 만한 한 줄을 내놓을 때까지 한 번 더 물어본다.
     *
     * <p>길이 초과를 자르지 않고 버리게 되면서 한 번 실패가 곧 fallback 이 됐다. fallback 은
     * "오늘 가보자" 같은 고정 문구라 자주 나오면 그게 더 티가 난다. 출력은 매번 달라지므로
     * 두 번째 시도는 대개 통과한다. 호출 자체가 터진 경우는 여기서 다시 걸지 않는다 —
     * `OpenRouterChatCaller` 가 이미 재시도한다.
     */
    public String generate(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        String prompt = buildPrompt(triggerType, teamName, scorerName);

        try {
            for (int attempt = 0; attempt < LLM_ATTEMPTS; attempt++) {
                String processed = postProcess(callLlm(prompt));
                if (processed != null) {
                    return processed;
                }
            }
            log.info("AI Seed 쓸 만한 응답을 못 받아 fallback 사용: trigger={}", triggerType);
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
                "max_tokens", MAX_TOKENS
        );

        OpenRouterChatResponse response = chatCaller.call(body, REQUEST_TIMEOUT);
        if (response == null) {
            return null;
        }
        return response.getText();
    }

    /**
     * 모델 출력을 응원톡 한 줄로 다듬는다. 쓸 수 없으면 null 을 돌려 fallback 을 타게 한다.
     *
     * <p>길이 초과를 **자르지 않고 버리는** 게 핵심이다. 예전에는 `substring` 으로 잘랐는데,
     * 팀 이름이 12자를 넘는 팀(운영 122개 중 29개)에서는 모델이 30자를 쉽게 넘겨
     * "서울시립대학교 남자축구 대표팀 오늘 날씨 좋아서 기분" 처럼 문장 중간에서 끊긴
     * 응원톡이 그대로 노출됐다. 운영 AI seed 121건 중 13건이 정확히 상한 길이였다.
     * 잘린 문장보다 fallback 한 줄이 낫다.
     *
     * <p>첫 줄만 쓰는 것도 같은 이유다. "한 문장만 출력" 을 지시해도 모델이 후보를 여러 줄
     * 나열할 때가 있고, 그러면 응원톡 하나에 메시지 다섯 개가 들어간다(2026-05-07 에 11건).
     */
    private String postProcess(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String trimmed = raw.strip().lines().findFirst().orElse("").strip();
        trimmed = stripWrappingQuotes(trimmed);
        trimmed = LEADING_BULLET.matcher(trimmed).replaceFirst("").strip();

        if (trimmed.isBlank() || bannedSoloReactions.contains(trimmed)) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            log.debug("AI Seed 길이 초과로 버림: {}자 > {}자, message={}", trimmed.length(), maxLength, trimmed);
            return null;
        }

        return trimmed;
    }

    private String stripWrappingQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).strip();
        }
        return value;
    }

    /**
     * 모델을 못 쓸 때 내보내는 문장. 팀 이름이 길면 여기서도 쓰지 않는다 —
     * "한국외국어대학교 남자축구 대표팀 가자" 는 사람이 치는 말이 아니다.
     */
    private String fallback(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        if (triggerType == AiSeedTriggerType.GOAL && scorerName != null) {
            return scorerName + " 좋았다";
        }
        if (triggerType == AiSeedTriggerType.OWN_GOAL) {
            return "이건 예상 못했네";
        }
        if (teamName.length() > TEAM_NAME_TOO_LONG) {
            return "오늘 가보자";
        }
        return teamName + " 가자";
    }

    private String buildPrompt(AiSeedTriggerType triggerType, String teamName, String scorerName) {
        String template = switch (triggerType) {
            case SCHEDULED -> scheduledPrompt;
            case SECOND_HALF_START -> secondHalfPrompt;
            case GOAL -> goalPrompt;
            case OWN_GOAL -> ownGoalPrompt;
        };

        String result = template.replace("{team_name}", teamName);
        if (scorerName != null) {
            result = result.replace("{scorer_name}", scorerName);
        }
        if (teamName.length() > TEAM_NAME_TOO_LONG) {
            result += System.lineSeparator()
                    + "- 팀 이름이 길다. 메시지에 팀 이름을 쓰지 말고 경기 이야기만 해라.";
        }
        return result;
    }
}