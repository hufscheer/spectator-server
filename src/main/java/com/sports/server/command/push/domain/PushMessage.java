package com.sports.server.command.push.domain;

import java.util.Map;

public record PushMessage(
        String title,
        String body,
        Map<String, String> data
) {
    public static final String URL_KEY = "url";
    public static final String GAME_ID_KEY = "gameId";
    public static final String TYPE_KEY = "type";

    public static PushMessage of(PushType type, Long gameId, String url, String title, String body) {
        return new PushMessage(title, body, Map.of(
                URL_KEY, url,
                GAME_ID_KEY, String.valueOf(gameId),
                TYPE_KEY, type.name()
        ));
    }

    public String url() {
        return data.get(URL_KEY);
    }
}
