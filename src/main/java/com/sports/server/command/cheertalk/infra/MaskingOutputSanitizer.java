package com.sports.server.command.cheertalk.infra;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaskingOutputSanitizer {

    private static final int LENGTH_BUFFER = 50;
    private static final int LENGTH_MULTIPLIER = 3;
    private static final int NEWLINE_BUFFER = 2;

    static final List<String> DEFAULT_LEAK_MARKERS = List.of(
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

    private final List<String> leakMarkers;

    public MaskingOutputSanitizer(
            @Value("${masking.leak-markers:}") List<String> leakMarkers
    ) {
        List<String> filtered = leakMarkers.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();
        this.leakMarkers = filtered.isEmpty() ? DEFAULT_LEAK_MARKERS : filtered;
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
        if (isModifiedWithoutMask(original, stripped)) {
            return original;
        }
        return stripped;
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
