package com.sports.server.command.push.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.CommonQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.push.domain.PushBroadcast;
import com.sports.server.command.push.domain.PushDeviceRepository;
import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushRecipient;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.GameProgressType;
import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.command.timeline.domain.TimelineRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 타임라인 기록 하나를 알림 메시지와 수신 기기 목록으로 바꾼다. 네트워크 호출을 하지 않으므로
 * 조회 트랜잭션 안에서 끝내고, 전송은 트랜잭션 밖에서 한다.
 */
@Component
@RequiredArgsConstructor
public class PushBroadcastFactory {

    private final TimelineRepository timelineRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushMessageFactory messageFactory;

    @Transactional(readOnly = true)
    public Optional<PushBroadcast> create(Long timelineId) {
        return timelineRepository.findById(timelineId)
                .flatMap(this::broadcastOf);
    }

    private Optional<PushBroadcast> broadcastOf(Timeline timeline) {
        return messageOf(timeline)
                .map(message -> new PushBroadcast(message, followersOf(timeline.getGame())));
    }

    private Optional<PushMessage> messageOf(Timeline timeline) {
        Game game = timeline.getGame();
        if (!hasBothTeams(game)) {
            return Optional.empty();
        }
        if (timeline instanceof GameProgressTimeline progress) {
            return progressMessage(game, progress);
        }
        if (game.getSportType() != SportType.SOCCER) {
            return Optional.empty();
        }
        if (timeline instanceof ScoreTimeline score) {
            return Optional.of(messageFactory.goal(game, score));
        }
        if (timeline instanceof OwnGoalTimeline ownGoal) {
            return Optional.of(messageFactory.goal(game, ownGoal));
        }
        return Optional.empty();
    }

    private Optional<PushMessage> progressMessage(Game game, GameProgressTimeline progress) {
        if (isGameStart(progress)) {
            return Optional.of(messageFactory.gameStart(game));
        }
        if (progress.getGameProgressType() == GameProgressType.GAME_END) {
            return Optional.of(messageFactory.gameEnd(game));
        }
        return Optional.empty();
    }

    private boolean isGameStart(GameProgressTimeline progress) {
        return progress.getGameProgressType() == GameProgressType.QUARTER_START
                && progress.getPreviousQuarter() == CommonQuarter.PRE_GAME;
    }

    // 팀이 지워진 경기는 팀 이름을 알 수 없어 알리지 않는다
    private boolean hasBothTeams(Game game) {
        return game.getGameTeams().size() == Game.MINIMUM_TEAMS
                && game.getGameTeams().stream().allMatch(GameTeam::hasTeam);
    }

    private List<PushRecipient> followersOf(Game game) {
        List<Long> teamIds = game.getGameTeams().stream().map(GameTeam::getTeamId).toList();
        return pushDeviceRepository.findFollowers(teamIds, game.getId()).stream()
                .map(PushRecipient::from)
                .toList();
    }
}
