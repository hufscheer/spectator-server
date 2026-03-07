package com.sports.server.command.nl.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiFunctionCallResponse(List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text, FunctionCall functionCall) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FunctionCall(String name, Map<String, Object> args) {
    }

    public boolean hasFunctionCall() {
        if (candidates == null || candidates.isEmpty()) return false;
        List<Part> parts = candidates.get(0).content().parts();
        if (parts == null || parts.isEmpty()) return false;
        return parts.get(0).functionCall() != null;
    }

    public FunctionCall getFunctionCall() {
        if (candidates == null || candidates.isEmpty()) return null;
        List<Part> parts = candidates.get(0).content().parts();
        if (parts == null || parts.isEmpty()) return null;
        return parts.get(0).functionCall();
    }

    public String getText() {
        if (candidates == null || candidates.isEmpty()) return "";
        List<Part> parts = candidates.get(0).content().parts();
        if (parts == null || parts.isEmpty()) return "";
        return parts.get(0).text() != null ? parts.get(0).text() : "";
    }
}
