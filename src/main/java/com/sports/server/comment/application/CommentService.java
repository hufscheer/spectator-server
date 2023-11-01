package com.sports.server.comment.application;

import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.comment.dto.response.CommentResponseDto;
import com.sports.server.game.application.GameService;
import com.sports.server.game.application.GameTeamService;
import com.sports.server.game.domain.Game;
import com.sports.server.game.domain.GameTeam;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    private final GameTeamService gameTeamService;

    private final GameService gameService;

    @Transactional
    public void register(final CommentRequestDto commentRequestDto) {
        GameTeam team = gameTeamService.findGameTeamWithId(commentRequestDto.getGameTeamId());
        commentRepository.save(commentRequestDto.toEntity(team));
    }

    public List<CommentResponseDto> getAllCommentsWithGameId(final Long gameId) {
        Game game = gameService.findGameWithId(gameId);
        return commentRepository.getAllByGameOrderByCreatedAtDesc(game).stream().map(CommentResponseDto::new).toList();
    }
}
