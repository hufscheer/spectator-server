package com.sports.server.command.nl.dto;

import com.sports.server.command.player.domain.Player;
import java.util.List;

public record NlCheckDuplicatesResponse(
        List<DuplicatePlayer> duplicates
) {
    public record DuplicatePlayer(
            String studentNumber,
            String name
    ) {
        public static DuplicatePlayer from(Player player) {
            return new DuplicatePlayer(player.getStudentNumber(), player.getName());
        }
    }
}
