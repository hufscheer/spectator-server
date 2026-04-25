package com.sports.server.common.infra.openrouter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterChatResponse(List<Choice> choices) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content, List<ToolCall> tool_calls) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ToolCall(FunctionPayload function) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FunctionPayload(String name, String arguments) {
    }

    public boolean hasToolCall() {
        if (choices == null || choices.isEmpty()) {
            return false;
        }
        Message message = choices.get(0).message();
        return message != null && message.tool_calls() != null && !message.tool_calls().isEmpty();
    }

    public String getText() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Message message = choices.get(0).message();
        return message == null ? null : message.content();
    }

    public <T> T getArgsAs(ObjectMapper mapper, Class<T> clazz) {
        if (!hasToolCall()) {
            return null;
        }
        String arguments = choices.get(0).message().tool_calls().get(0).function().arguments();
        try {
            return mapper.readValue(arguments, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
