package com.sports.server.command.cheertalk.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {
    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }

    public String getFirstText() {
        if (candidates != null && !candidates.isEmpty()) {
            List<Part> parts = candidates.get(0).content().parts();
            if (parts != null && !parts.isEmpty()) {
                return parts.get(0).text();
            }
        }
        return "";
    }
}