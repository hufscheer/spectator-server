package com.sports.server.command.nl.dto;

public record NlExtractResponse(String text, NlSourceType sourceType, boolean truncated) {
}
