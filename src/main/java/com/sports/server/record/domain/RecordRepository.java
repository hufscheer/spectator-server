package com.sports.server.record.domain;

import com.sports.server.game.domain.Game;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface RecordRepository extends Repository<Record, Long> {

    List<Record> findAllByGame(final Game game);

}
