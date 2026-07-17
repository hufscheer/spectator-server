package com.sports.server.command.bracket.domain;

import com.sports.server.command.bracket.exception.BracketErrorMessages;
import com.sports.server.command.game.domain.Game;
import com.sports.server.command.game.domain.GameResult;
import com.sports.server.command.game.domain.GameState;
import com.sports.server.command.game.domain.GameTeam;
import com.sports.server.command.league.domain.League;
import com.sports.server.command.league.domain.Round;
import com.sports.server.command.team.domain.Team;
import com.sports.server.common.exception.BadRequestException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * 리그의 대진표 트리. round/matchNumber 수학으로 부모-자식 매치를 유도한다.
 * round R 의 match m 은 아래 라운드(2R)의 match 2m-1, 2m 승자끼리 대결한다.
 * 팀 배치(team1/team2)는 1라운드에만 저장되고, 상위 라운드 슬롯은 경기 결과와 부전승으로부터 유도된다.
 */
public class Bracket {

    private final int size;
    private final Map<Integer, Map<Integer, BracketMatch>> matchesByRound;
    private final Map<Integer, Team> placements;

    private Bracket(final int size,
                    final Map<Integer, Map<Integer, BracketMatch>> matchesByRound,
                    final Map<Integer, Team> placements) {
        this.size = size;
        this.matchesByRound = matchesByRound;
        this.placements = placements;
    }

    public static List<BracketMatch> generate(final League league, final int size,
                                              final Map<Integer, Team> placements) {
        List<BracketMatch> matches = new ArrayList<>();
        for (int roundNumber = size; roundNumber >= Round.FINAL.getNumber(); roundNumber /= 2) {
            Round round = Round.from(roundNumber);
            for (int matchNumber = 1; matchNumber <= roundNumber / 2; matchNumber++) {
                BracketMatch match = new BracketMatch(league, round, matchNumber);
                if (roundNumber == size) {
                    match.placeTeams(placements.get(matchNumber * 2 - 1), placements.get(matchNumber * 2));
                }
                matches.add(match);
            }
        }
        return matches;
    }

    public static Bracket from(final List<BracketMatch> matches) {
        if (matches.isEmpty()) {
            throw new BadRequestException(BracketErrorMessages.BRACKET_NOT_FOUND);
        }
        Map<Integer, Map<Integer, BracketMatch>> matchesByRound = new HashMap<>();
        for (BracketMatch match : matches) {
            matchesByRound.computeIfAbsent(match.getRound().getNumber(), key -> new HashMap<>())
                    .put(match.getMatchNumber(), match);
        }
        int size = matchesByRound.keySet().stream().mapToInt(Integer::intValue).max().orElseThrow();

        Map<Integer, Team> placements = new HashMap<>();
        for (BracketMatch match : matchesByRound.get(size).values()) {
            if (match.getTeam1() != null) {
                placements.put(match.getMatchNumber() * 2 - 1, match.getTeam1());
            }
            if (match.getTeam2() != null) {
                placements.put(match.getMatchNumber() * 2, match.getTeam2());
            }
        }
        return new Bracket(size, matchesByRound, placements);
    }

    public int getSize() {
        return size;
    }

    public List<Integer> roundNumbers() {
        List<Integer> roundNumbers = new ArrayList<>();
        for (int roundNumber = size; roundNumber >= Round.FINAL.getNumber(); roundNumber /= 2) {
            roundNumbers.add(roundNumber);
        }
        return roundNumbers;
    }

    public List<BracketMatch> matchesOf(final int roundNumber) {
        return matchesByRound.getOrDefault(roundNumber, Map.of()).values().stream()
                .sorted(Comparator.comparingInt(BracketMatch::getMatchNumber))
                .toList();
    }

    /**
     * 두 팀이 해당 라운드에서 만나는 매치를 찾는다.
     * 두 팀 모두 1라운드에 배치되어 있고, 대진표상 해당 라운드에서 서로 다른 사이드로 만나는 경우에만 존재한다.
     */
    public Optional<BracketMatch> findMeetingMatch(final Round round, final Long teamId1, final Long teamId2) {
        int roundNumber = round.getNumber();
        if (!matchesByRound.containsKey(roundNumber)) {
            return Optional.empty();
        }
        Integer position1 = positionOf(teamId1);
        Integer position2 = positionOf(teamId2);
        if (position1 == null || position2 == null) {
            return Optional.empty();
        }
        if (meetingMatchNumber(roundNumber, position1) != meetingMatchNumber(roundNumber, position2)) {
            return Optional.empty();
        }
        if (sideBlockOf(roundNumber, position1) == sideBlockOf(roundNumber, position2)) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                matchesByRound.get(roundNumber).get(meetingMatchNumber(roundNumber, position1)));
    }

    public Team slot1Of(final BracketMatch match) {
        return slotOf(match, 1);
    }

    public Team slot2Of(final BracketMatch match) {
        return slotOf(match, 2);
    }

    /**
     * 매치의 다음 라운드 진출팀. 경기 승자 → 부전승 순으로 판단하며 미확정이면 null.
     */
    public Team advancerOf(final BracketMatch match) {
        Team winner = winnerOf(match);
        if (winner != null) {
            return winner;
        }
        if (match.isLinked()) {
            return null;
        }
        Team slot1 = slotOf(match, 1);
        Team slot2 = slotOf(match, 2);
        if (slot1 != null && slot2 == null && !subtreeHasTeam(match, 2)) {
            return slot1;
        }
        if (slot2 != null && slot1 == null && !subtreeHasTeam(match, 1)) {
            return slot2;
        }
        return null;
    }

    public Team winnerOf(final BracketMatch match) {
        Game game = match.getGame();
        if (game == null || game.getState() != GameState.FINISHED) {
            return null;
        }
        return game.getGameTeams().stream()
                .filter(gameTeam -> gameTeam.getResult() == GameResult.WIN)
                .map(GameTeam::getTeam)
                .findAny()
                .orElse(null);
    }

    private Team slotOf(final BracketMatch match, final int side) {
        if (isFirstRound(match)) {
            return side == 1 ? match.getTeam1() : match.getTeam2();
        }
        BracketMatch feeder = feederOf(match, side);
        if (feeder == null) {
            return null;
        }
        return advancerOf(feeder);
    }

    private boolean isFirstRound(final BracketMatch match) {
        return match.getRound().getNumber() == size;
    }

    private BracketMatch feederOf(final BracketMatch match, final int side) {
        int feederRoundNumber = match.getRound().getNumber() * 2;
        int feederMatchNumber = match.getMatchNumber() * 2 - (side == 1 ? 1 : 0);
        return matchesByRound.getOrDefault(feederRoundNumber, Map.of()).get(feederMatchNumber);
    }

    // 해당 사이드로 이어지는 1라운드 구간에 배치된 팀이 하나라도 있는지 (부전승 판단용)
    private boolean subtreeHasTeam(final BracketMatch match, final int side) {
        int blockSize = size / match.getRound().getNumber();
        int start = (match.getMatchNumber() - 1) * blockSize * 2 + (side - 1) * blockSize + 1;
        return IntStream.range(start, start + blockSize).anyMatch(placements::containsKey);
    }

    private Integer positionOf(final Long teamId) {
        return placements.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(teamId))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }

    // 1라운드 position 의 팀이 해당 라운드에서 배정되는 매치 번호
    private int meetingMatchNumber(final int roundNumber, final int position) {
        return ceilDiv(position * roundNumber, size * 2);
    }

    // 매치 내 좌우 슬롯을 구분하는 전역 블록 번호 (같은 값이면 같은 사이드)
    private int sideBlockOf(final int roundNumber, final int position) {
        return ceilDiv(position * roundNumber, size);
    }

    private static int ceilDiv(final int dividend, final int divisor) {
        return (dividend + divisor - 1) / divisor;
    }
}
