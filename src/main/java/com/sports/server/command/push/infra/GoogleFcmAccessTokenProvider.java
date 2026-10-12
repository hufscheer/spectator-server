package com.sports.server.command.push.infra;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.UncheckedIOException;

public class GoogleFcmAccessTokenProvider implements FcmAccessTokenProvider {

    private final GoogleCredentials credentials;

    public GoogleFcmAccessTokenProvider(GoogleCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String accessToken() {
        try {
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
