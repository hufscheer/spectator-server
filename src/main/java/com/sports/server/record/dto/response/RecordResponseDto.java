package com.sports.server.record.dto.response;

import com.sports.server.common.Constants;
import com.sports.server.record.domain.Record;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
public class RecordResponseDto {

    private Long id;

    private Long teamId;

    private String playerName;

    private int score;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT)
    private LocalDateTime scoredAt;

    public RecordResponseDto(final Record record) {
        this.id = record.getId();
        this.teamId = record.getGameTeam().getId();
        this.playerName = record.getGameTeamPlayer().getName();
        this.score = record.getScore();
        this.scoredAt = record.getScoredAt();
    }
}
