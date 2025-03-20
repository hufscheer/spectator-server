package com.sports.server.command.leagueteam.domain;

import com.sports.server.common.exception.CustomException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;

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
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 팀 컬러입니다."));
    }
}

