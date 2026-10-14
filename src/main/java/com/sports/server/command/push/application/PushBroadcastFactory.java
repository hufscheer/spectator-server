package com.sports.server.command.push.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.BasketballQuarter;
import com.sports.server.command.league.domain.CommonQuarter;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.command.push.domain.PushBroadcast;
import com.sports.server.command.push.domain.PushDevice;
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
import java.util.Set;
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

    private static final Set<Quarter> NOTIFIABLE_QUARTER_ENDS = Set.of(
            BasketballQuarter.FIRST_QUARTER, BasketballQuarter.SECOND_QUARTER, BasketballQuarter.THIRD_QUARTER,
            SoccerQuarter.FIRST_HALF);

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
        if (isNotifiableQuarterEnd(progress)) {
            return Optional.of(messageFactory.quarterEnd(game, progress));
        }
        if (isSoccerSecondHalfStart(game, progress)) {
            return Optional.of(messageFactory.quarterStart(game, progress));
        }
        return Optional.empty();
    }

    // 농구 4쿼터와 연장전, 축구 후반전과 연장전 종료는 곧바로 경기 종료가 이어지므로 알리지 않는다.
    // 축구는 전반 종료만 알린다
    private boolean isNotifiableQuarterEnd(GameProgressTimeline progress) {
        return progress.getGameProgressType() == GameProgressType.QUARTER_END
                && NOTIFIABLE_QUARTER_ENDS.contains(progress.getRecordedQuarter());
    }

    private boolean isSoccerSecondHalfStart(Game game, GameProgressTimeline progress) {
        return game.getSportType() == SportType.SOCCER
                && progress.getGameProgressType() == GameProgressType.QUARTER_START
                && progress.getRecordedQuarter() == SoccerQuarter.SECOND_HALF;
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

    // 팀이나 경기를 팔로우한 기기에만 보낸다. 학교 전체 알림은 고황체전부터 쓰지 않는다(팀 알림만 운영)
    private List<PushRecipient> followersOf(Game game) {
        List<Long> teamIds = game.getGameTeams().stream().map(GameTeam::getTeamId).toList();
        List<PushDevice> followers = pushDeviceRepository.findFollowers(teamIds, game.getId());
        return followers.stream()
                .map(PushRecipient::from)
                .toList();
    }
}
