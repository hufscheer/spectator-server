package com.sports.server.command.cheertalk.infra;

import java.util.List;

/**
 * LLM 응답 텍스트에서 추론/메타 코멘트가 새어 나온 경우를 탐지해 원문으로 폴백한다.
 * "의심스러우면 마스킹하지 않는다"는 기존 정책의 출력단 방어선.
 */
public final class MaskingOutputSanitizer {

    private static final int LENGTH_BUFFER = 50;
    private static final int LENGTH_MULTIPLIER = 3;
    private static final int NEWLINE_BUFFER = 2;

    private static final List<String> LEAK_MARKERS = List.of(
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

    private MaskingOutputSanitizer() {
    }

    public static String sanitize(String original, String modelOutput) {
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

    private static boolean isModifiedWithoutMask(String original, String modelOutput) {
        return !modelOutput.equals(original) && !modelOutput.contains("*");
    }

    private static boolean isTooLong(String original, String modelOutput) {
        return modelOutput.length() > original.length() * LENGTH_MULTIPLIER + LENGTH_BUFFER;
    }

    private static boolean hasUnexpectedNewlines(String original, String modelOutput) {
        return countNewlines(modelOutput) > countNewlines(original) + NEWLINE_BUFFER;
    }

    private static int countNewlines(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private static boolean containsLeakMarker(String modelOutput) {
        for (String marker : LEAK_MARKERS) {
            if (modelOutput.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
