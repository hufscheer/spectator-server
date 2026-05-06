package com.sports.server.command.cheertalk.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LLM 마스킹 호출 전에 명백히 정상인 메시지를 걸러낸다.
 * 자모 단독 메시지는 비속어 초성일 수 있으므로 화이트리스트에 등록된 케이스만 스킵한다.
 */
@Component
public class MaskingPreFilter {

    private final Set<String> recommendedMessages;
    private final Set<String> positiveConsonants;

    public MaskingPreFilter(
            @Value("${masking.recommended-messages:}") List<String> recommendedMessages,
            @Value("${masking.positive-consonants:}") List<String> positiveConsonants
    ) {
        this.recommendedMessages = toNormalizedSet(recommendedMessages);
        this.positiveConsonants = toNormalizedSet(positiveConsonants);
    }

    public boolean canSkip(String content) {
        if (content == null) {
            return true;
        }
        String trimmed = Normalizer.normalize(content, Normalizer.Form.NFC).strip();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (recommendedMessages.contains(trimmed)) {
            return true;
        }
        if (positiveConsonants.contains(trimmed)) {
            return true;
        }
        return !containsAnyKorean(trimmed);
    }

    private static Set<String> toNormalizedSet(List<String> values) {
        return values.stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .map(s -> Normalizer.normalize(s, Normalizer.Form.NFC))
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean containsAnyKorean(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (isKorean(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isKorean(char c) {
        if (c >= 0xAC00 && c <= 0xD7A3) return true;
        if (c >= 0x1100 && c <= 0x11FF) return true;
        if (c >= 0x3130 && c <= 0x318F) return true;
        if (c >= 0xA960 && c <= 0xA97F) return true;
        if (c >= 0xD7B0 && c <= 0xD7FF) return true;
        return false;
    }
}
