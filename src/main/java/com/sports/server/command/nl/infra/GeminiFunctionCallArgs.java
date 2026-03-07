package com.sports.server.command.nl.infra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sports.server.command.nl.dto.NlParseResult.ParsedPlayer;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiFunctionCallArgs(List<ParsedPlayer> players) {
}
