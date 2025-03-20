package com.sports.server.command.leagueteam.domain;

import java.util.Arrays;

public enum TeamColor {
    PINK("FF9A9E"),
    BLUE("8CBAFF"),
    ORANGE("FFA788"),
    GRAY_BLUE("99A8CC"),
    YELLOW("FFD479"),
    LIGHT_GRAY("B8C0CC");

    private final String hexCode;

    TeamColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }

    public static TeamColor fromHexCode(String hexCode) {
        return Arrays.stream(TeamColor.values())
                .filter(color -> color.getHexCode().equals(hexCode))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀 컬러입니다."));
    }
}

