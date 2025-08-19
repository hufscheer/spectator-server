package com.sports.server.command.team.domain;

import com.sports.server.command.team.exception.TeamErrorMessages;
import com.sports.server.common.exception.NotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Unit {
    ENGLISH("영어대학"),
    OCCIDENTAL_LANGUAGES("서양어대학"),
    ASIAN_LANGUAGES_AND_CULTURE("아시아언어문화대학"),
    CHINESE_STUDIES("중국학대학"),
    JAPANESE_STUDIES("일본어대학"),
    SOCIAL_SCIENCES("사회과학대학"),
    BUSINESS_AND_ECONOMICS("상경대학"),
    BUSINESS("경영대학"),
    EDUCATION("사범대학"),
    AI_CONVERGENCE("AI융합대학"),
    INTERNATIONAL_STUDIES("국제학부"),
    LANGUAGE_AND_DIPLOMACY("LD학부"),
    LANGUAGE_AND_TRADE("LT학부"),
    KOREAN_AS_A_FOREIGN_LANGUAGE("KFL학부"),
    LIBERAL_ARTS("자유전공학부"),
    ETC("기타");

    private final String name;

    public static Unit from(String englishName) {
        return Stream.of(values())
                .filter(u -> u.name().equals(englishName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(TeamErrorMessages.UNIT_NOT_FOUND_EXCEPTION));
    }
}
