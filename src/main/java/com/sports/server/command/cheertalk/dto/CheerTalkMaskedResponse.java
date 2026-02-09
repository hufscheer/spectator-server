package com.sports.server.command.cheertalk.dto;

public record CheerTalkMaskedResponse(
        boolean containsBadWord,
        String maskedContent
) {
}
