package com.sports.server.command.cheertalk.infra;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * LLM 응답 텍스트에서 추론/메타 코멘트가 새어 나온 경우를 탐지해 원문으로 폴백한다.
 * "의심스러우면 마스킹하지 않는다"는 기존 정책의 출력단 방어선.
 */
@Component
public class MaskingOutputSanitizer {

    private static final int LENGTH_BUFFER = 50;
    private static final int LENGTH_MULTIPLIER = 3;
    private static final int NEWLINE_BUFFER = 2;

    private static final List<String> DEFAULT_LEAK_MARKERS = List.of(
            "---",
            "처리하겠습니다",
            "본 답변은",
            "다음과 같이 처리",
            "필터링 범위",
            "범위를 벗어",
            "응원톡 필터링",
            "포함되어 있지 않아",
            "그대로 출력합니다",
            "마스킹하지 않",
            "마스킹할 필요"
    );

    /**
     * 욕설 초성 화이트리스트. gemini 프롬프트 [반드시 마스킹] 항목과 동기화 유지.
     * 짧은 긍정 초성(ㄱㅅ 등)이 욕설 초성(ㄱㅅㄲ)의 부분문자열일 때 잘못 복구하는 것을 방지한다.
     */
    private static final List<String> BANNED_PROFANITY_INITIALS = List.of(
            "ㅅㅂ", "ㄱㅅㄲ", "ㅈㄴ", "ㅄ", "ㅂㅅ"
    );

    private static final int LONG_POSITIVE_THRESHOLD = 3;
    private static final Pattern POSITIVE_TOKEN_PATTERN = Pattern.compile("[\\s,.!?~^]+");

    private final List<String> leakMarkers;
    private final Set<String> positiveConsonants;

    public MaskingOutputSanitizer(
            @Value("${masking.leak-markers:}") List<String> leakMarkers,
            @Value("${masking.positive-consonants:}") List<String> positiveConsonants
    ) {
        List<String> filtered = leakMarkers.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::strip)
                .toList();
        this.leakMarkers = filtered.isEmpty() ? DEFAULT_LEAK_MARKERS : filtered;
        this.positiveConsonants = positiveConsonants.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::strip)
                .map(s -> Normalizer.normalize(s, Normalizer.Form.NFC))
                .collect(Collectors.toUnmodifiableSet());
    }

    public String sanitize(String original, String modelOutput) {
        if (modelOutput == null) {
            return original;
        }
        String stripped = modelOutput.strip();
        if (stripped.isEmpty()) {
            return original;
        }
        if (isTooLong(original, stripped)) {
            return original;
        }
        if (hasUnexpectedNewlines(original, stripped)) {
            return original;
        }
        if (containsLeakMarker(stripped)) {
            return original;
        }
        if (stripped.equals(original)) {
            return stripped;
        }
        if (isModifiedWithoutMask(original, stripped)) {
            return original;
        }
        if (lostPositiveConsonant(original, stripped)) {
            return original;
        }
        return stripped;
    }

    /**
     * 원문에 있던 긍정 초성이 출력에서 사라졌으면 모델이 잘못 마스킹한 것으로 보고 원문을 복구한다.
     * 단 두 가지 예외로 복구를 보류한다:
     * 1) 원문에 욕설 초성이 함께 있으면 마스킹이 정당할 수 있어 신뢰한다.
     * 2) 짧은 긍정 초성(ㄱㅅ 등)은 욕설 초성(ㄱㅅㄲ)의 부분문자열일 수 있어 토큰 단독 일치만 보호한다.
     */
    private boolean lostPositiveConsonant(String original, String modelOutput) {
        if (positiveConsonants.isEmpty()) {
            return false;
        }
        String originalNFC = Normalizer.normalize(original, Normalizer.Form.NFC);
        if (containsBannedInitial(originalNFC)) {
            return false;
        }
        String outputNFC = Normalizer.normalize(modelOutput, Normalizer.Form.NFC);
        for (String positiveConsonant : positiveConsonants) {
            if (outputNFC.contains(positiveConsonant)) {
                continue;
            }
            if (isProtectedInOriginal(originalNFC, positiveConsonant)) {
                return true;
            }
        }
        return false;
    }

    private boolean isProtectedInOriginal(String original, String positiveConsonant) {
        if (positiveConsonant.length() >= LONG_POSITIVE_THRESHOLD) {
            return original.contains(positiveConsonant);
        }
        return containsAsToken(original, positiveConsonant);
    }

    private boolean containsBannedInitial(String original) {
        for (String banned : BANNED_PROFANITY_INITIALS) {
            if (original.contains(banned)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAsToken(String text, String token) {
        for (String t : POSITIVE_TOKEN_PATTERN.split(text)) {
            if (t.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean isModifiedWithoutMask(String original, String modelOutput) {
        return !modelOutput.equals(original) && !modelOutput.contains("*");
    }

    private boolean isTooLong(String original, String modelOutput) {
        return modelOutput.length() > original.length() * LENGTH_MULTIPLIER + LENGTH_BUFFER;
    }

    private boolean hasUnexpectedNewlines(String original, String modelOutput) {
        return countNewlines(modelOutput) > countNewlines(original) + NEWLINE_BUFFER;
    }

    private int countNewlines(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private boolean containsLeakMarker(String modelOutput) {
        for (String marker : leakMarkers) {
            if (modelOutput.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
