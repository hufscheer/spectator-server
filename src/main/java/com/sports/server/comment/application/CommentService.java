package com.sports.server.comment.application;

import com.sports.server.comment.domain.CommentRepository;
import com.sports.server.comment.dto.request.CommentRequestDto;
import com.sports.server.game.application.GameService;
import com.sports.server.game.domain.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    private final GameService gameService;

    @Transactional
    public void register(final CommentRequestDto commentRequestDto) {
        Game game = gameService.findGameWithId(commentRequestDto.getGameId());
        commentRepository.save(commentRequestDto.toEntity(game));
    }
}
