package com.sports.server.command.push.application;

import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.Quarter;
import com.sports.server.command.league.domain.SoccerQuarter;
import com.sports.server.command.push.domain.PushMessage;
import com.sports.server.command.push.domain.PushType;
import com.sports.server.command.timeline.domain.GameProgressTimeline;
import com.sports.server.command.timeline.domain.OwnGoalTimeline;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import org.springframework.stereotype.Component;

@Component
public class PushMessageFactory {

    private static final String URL_FORMAT = "/org/%d/%s/games/%d";

    // 축구는 경기 시작을 전반 시작으로 알린다
    public PushMessage gameStart(Game game) {
        String title = switch (game.getSportType()) {
            case SOCCER -> "⚽ 전반 시작";
            case BASKETBALL -> "🏀 경기 시작";
        };
        return message(PushType.GAME_START, game, title, matchLine(game));
    }

    public PushMessage goal(Game game, ScoreTimeline timeline) {
        GameTeam scoringTeam = timeline.getScorer().getGameTeam();
        return goal(game, timeline, scoringTeam, timeline.getSnapshotScore1(), timeline.getSnapshotScore2());
    }

    // 자책골은 상대 팀의 득점으로 알린다
    public PushMessage goal(Game game, OwnGoalTimeline timeline) {
        return goal(game, timeline, timeline.getOpponentGameTeam(),
                timeline.getSnapshotScore1(), timeline.getSnapshotScore2());
    }

    // 알림을 보내는 시점에는 경기 점수가 더 움직였을 수 있어 기록에 남은 점수를 쓴다
    private PushMessage goal(Game game, Timeline timeline, GameTeam scoringTeam, int score1, int score2) {
        String title = String.format("%s %s 골!", iconOf(game), scoringTeam.getTeamName());
        String body = String.format("%s (%s %d')", scoreLine(game, score1, score2),
                timeline.getRecordedQuarter().getDisplayName(), timeline.getRecordedAt());
        return message(PushType.GOAL, game, title, body);
    }

    // 경기 종료와 같이 경기의 현재 점수를 쓴다. 진행 기록에는 점수 스냅샷이 없다
    public PushMessage quarterEnd(Game game, GameProgressTimeline timeline) {
        String body = scoreLine(game, game.getTeam1().getScore(), game.getTeam2().getScore());
        return message(PushType.QUARTER_END, game,
                iconOf(game) + " " + quarterLabel(timeline.getRecordedQuarter()) + " 종료", body);
    }

    // 현재는 축구 후반전 시작에만 쓴다. 경기 시작처럼 대진과 대회명을 보인다
    public PushMessage quarterStart(Game game, GameProgressTimeline timeline) {
        return message(PushType.QUARTER_START, game,
                iconOf(game) + " " + quarterLabel(timeline.getRecordedQuarter()) + " 시작", matchLine(game));
    }

    // 승리한 팀은 제목에, 최종 점수는 본문에 둔다
    public PushMessage gameEnd(Game game) {
        String body = scoreLine(game, game.getTeam1().getScore(), game.getTeam2().getScore()) + pkSuffix(game);
        return message(PushType.GAME_END, game, iconOf(game) + " 경기 종료" + winnerSuffix(game), body);
    }

    // 알림 문구는 화면의 쿼터 이름(전반전·후반전)보다 짧게 쓴다
    private String quarterLabel(Quarter quarter) {
        if (quarter == SoccerQuarter.FIRST_HALF) {
            return "전반";
        }
        if (quarter == SoccerQuarter.SECOND_HALF) {
            return "후반";
        }
        return quarter.getDisplayName();
    }

    private String matchLine(Game game) {
        return String.format("%s vs %s · %s %s",
                game.getTeam1().getTeamName(), game.getTeam2().getTeamName(),
                game.getLeague().getName(), game.getRound().getDescription());
    }

    // 알림 목록에서 종목을 바로 알아보게 제목 앞에 붙인다
    private String iconOf(Game game) {
        return switch (game.getSportType()) {
            case SOCCER -> "⚽";
            case BASKETBALL -> "🏀";
        };
    }

    private String scoreLine(Game game, int score1, int score2) {
        return String.format("%s %d : %d %s",
                game.getTeam1().getTeamName(), score1, score2, game.getTeam2().getTeamName());
    }

    private String pkSuffix(Game game) {
        if (!Boolean.TRUE.equals(game.getIsPkTaken())) {
            return "";
        }
        return String.format(" (승부차기 %d:%d)", game.getTeam1().getPkScore(), game.getTeam2().getPkScore());
    }

    private String winnerSuffix(Game game) {
        return game.getGameTeams().stream()
                .filter(gameTeam -> gameTeam.getResult() == GameResult.WIN)
                .findFirst()
                .map(GameTeam::getTeamName)
                .map(name -> " · " + name + " 승리")
                .orElse("");
    }

    private PushMessage message(PushType type, Game game, String title, String body) {
        String url = String.format(URL_FORMAT, game.getOrganizationId(),
                game.getSportType().name().toLowerCase(), game.getId());
        return PushMessage.of(type, game.getId(), url, title, body);
    }
}
