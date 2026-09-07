package com.sports.server.command.bracket.dto;

import java.util.List;

public class BracketRequest {

    public record Save(
            int size,
            List<Entry> entries
    ) {
    }

    public record Entry(
            int position,
            Long teamId
    ) {
    }
}
