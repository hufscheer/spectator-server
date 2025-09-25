package com.sports.server.query.repository;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import java.util.List;

public interface CheerTalkDynamicRepository {

    List<CheerTalk> findAllUnblocked(Long cursor, Integer size);

    List<CheerTalk> findAllBlocked(Long cursor, Integer size);

    List<CheerTalk> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size);

    List<CheerTalk> findReportedCheerTalksByLeagueId(Long leagueId, Long cursor, Integer size);

    List<CheerTalk> findUnblockedCheerTalksByLeagueId(Long leagueId, Long cursor, Integer size);

    List<CheerTalk> findBlockedCheerTalksByLeagueId(Long leagueId, Long cursor, Integer size);
}
