package com.sports.server.command.cheertalk.infra;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LLM 마스킹 호출 전에 명백히 정상인 메시지를 걸러낸다.
 * 응원톡 도배의 상당수는 추천 문구나 짧은 응원 표현이라 LLM에 보낼 필요가 없다.
 *
 * 안전 원칙: 한국어 욕설 가능성이 0인 케이스만 스킵한다.
 * 비속어 초성(ㅅㅂ 등)은 자모만으로 구성될 수 있으므로 자모 단독 메시지는 LLM에 위임한다.
 */
@Component
public class MaskingPreFilter {

    /**
     * 프론트(`apps/spectator/.../cheer-talk-form.tsx`)의 RECOMMENDED_MESSAGES와 정확히 일치해야 한다.
     * 프론트에서 추천 문구를 변경하면 이 리스트도 함께 갱신해야 한다.
     */
    private static final Set<String> RECOMMENDED_MESSAGES = Set.of(
            "가즈아🔥",   // 가즈아🔥
            "나이스👍",   // 나이스👍
            "까비😭️" // 까비😭️
    ).stream().map(MaskingPreFilter::nfc).collect(Collectors.toUnmodifiableSet());

    /**
     * 응원/긍정 초성 — yml `[절대 마스킹 금지]` 항목 기반 정확 매치.
     */
    private static final Set<String> POSITIVE_CONSONANTS = Set.of(
            "ㅍㅇㅌ", "ㅎㅇㅌ", "ㅎㅇ",
            "ㄱㄱ", "ㄱㅅ",
            "ㅊㅋ", "ㄷㄷ", "ㄹㅇ", "ㅇㅈ",
            "ㄴㄴ", "ㅇㅇ"
    ).stream().map(MaskingPreFilter::nfc).collect(Collectors.toUnmodifiableSet());

    public boolean canSkip(String content) {
        if (content == null) {
            return true;
        }
        String trimmed = nfc(content).strip();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (RECOMMENDED_MESSAGES.contains(trimmed)) {
            return true;
        }
        if (POSITIVE_CONSONANTS.contains(trimmed)) {
            return true;
        }
        return !containsAnyHangul(trimmed);
    }

    private static String nfc(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    private boolean containsAnyHangul(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isHangul(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHangul(char c) {
        if (c >= 0xAC00 && c <= 0xD7A3) return true;   // 한글 음절
        if (c >= 0x1100 && c <= 0x11FF) return true;   // 한글 자모
        if (c >= 0x3130 && c <= 0x318F) return true;   // 한글 호환 자모
        if (c >= 0xA960 && c <= 0xA97F) return true;   // 한글 자모 확장-A
        if (c >= 0xD7B0 && c <= 0xD7FF) return true;   // 한글 자모 확장-B
        return false;
    }
}
