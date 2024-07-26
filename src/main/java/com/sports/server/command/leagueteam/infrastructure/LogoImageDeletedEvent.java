package com.sports.server.command.leagueteam.infrastructure;

public record LogoImageDeletedEvent(
        String keyOfImageUrl
) {
}
