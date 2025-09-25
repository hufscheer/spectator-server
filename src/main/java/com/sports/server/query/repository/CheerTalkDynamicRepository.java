package com.sports.server.query.repository;

import com.sports.server.command.cheertalk.domain.CheerTalk;
import java.util.List;

public interface CheerTalkDynamicRepository {

    List<CheerTalk> findByGameIdOrderByStartTime(Long gameId, Long cursor, Integer size);

    List<CheerTalk> findReportedCheerTalksByAdminId(Long adminId, Long cursor, Integer size);

    List<CheerTalk> findUnblockedCheerTalksByAdminId(Long adminId, Long cursor, Integer size);

    List<CheerTalk> findBlockedCheerTalksByAdminId(Long adminId, Long cursor, Integer size);
}
