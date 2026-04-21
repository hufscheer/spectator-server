package com.sports.server.command.nl.application;

import com.sports.server.command.nl.dto.NlParseResult;

import java.util.List;
import java.util.Map;

public interface NlClient {
    NlParseResult parsePlayers(String message, List<Map<String, String>> history, int studentNumberDigits);
}
