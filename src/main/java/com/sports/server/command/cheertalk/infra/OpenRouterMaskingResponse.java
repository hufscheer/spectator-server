package com.sports.server.command.cheertalk.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterMaskingResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content) {
    }

    public String getFirstContent() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Message message = choices.get(0).message();
        return message == null ? null : message.content();
    }
}
