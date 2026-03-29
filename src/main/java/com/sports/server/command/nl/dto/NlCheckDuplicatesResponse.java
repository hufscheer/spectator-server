package com.sports.server.command.nl.dto;

import com.sports.server.command.nl.dto.NlProcessResponse.PlayerPreview;
import com.sports.server.command.nl.dto.NlProcessResponse.Summary;
import java.util.List;

public record NlCheckDuplicatesResponse(
        List<PlayerPreview> players,
        Summary summary
) {
}
