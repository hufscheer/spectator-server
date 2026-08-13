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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * 리그의 대진표 트리. 부모-자식 매치를 저장하지 않고 round/matchNumber 값으로 계산해 찾는다.
 * round R 의 match m 은 아래 라운드(2R)의 match 2m-1, 2m 승자끼리 대결한다.
 * 팀 배치(team1/team2)는 1라운드에만 저장되고, 상위 라운드 슬롯은 경기 결과와 부전승으로부터 유도된다.
 * 3·4위전은 승자가 아닌 준결승 패자가 모이는 경기라 이 계산이 성립하지 않아, 트리와 분리해 부속 매치로 들고 있다.
 */
public class Bracket {

    public static final int TEAM1_SIDE = 1;
    public static final int TEAM2_SIDE = 2;

    private static final int TEAMS_PER_MATCH = 2;
    private static final int THIRD_PLACE_MATCH_NUMBER = 1;
    private static final int FIRST_SEMI_FINAL = 1;
    private static final int SECOND_SEMI_FINAL = 2;

    private final int size;
    private final Map<Integer, Map<Integer, BracketMatch>> matchesByRound;
    private final Map<Integer, Team> placements;
    private final BracketMatch thirdPlaceMatch;

    private Bracket(final int size,
                    final Map<Integer, Map<Integer, BracketMatch>> matchesByRound,
                    final Map<Integer, Team> placements,
                    final BracketMatch thirdPlaceMatch) {
        this.size = size;
        this.matchesByRound = matchesByRound;
        this.placements = placements;
        this.thirdPlaceMatch = thirdPlaceMatch;
    }

    public static List<BracketMatch> generate(final League league, final int size,
                                              final Map<Integer, Team> placements) {
        List<BracketMatch> matches = new ArrayList<>();
        for (int roundNumber = size; roundNumber >= Round.FINAL.getNumber(); roundNumber /= TEAMS_PER_MATCH) {
            matches.addAll(generateRound(league, size, roundNumber, placements));
        }
        if (needsThirdPlaceMatch(league, size)) {
            matches.add(new BracketMatch(league, Round.THIRD_PLACE_MATCH, THIRD_PLACE_MATCH_NUMBER));
        }
        return matches;
    }

    private static boolean needsThirdPlaceMatch(final League league, final int size) {
        return league.isThirdPlaceMatchEnabled() && size >= Round.SEMI_FINAL.getNumber();
    }

    private static List<BracketMatch> generateRound(final League league, final int size, final int roundNumber,
                                                    final Map<Integer, Team> placements) {
        Round round = Round.from(roundNumber);
        List<BracketMatch> matches = new ArrayList<>();
        for (int matchNumber = 1; matchNumber <= roundNumber / TEAMS_PER_MATCH; matchNumber++) {
            BracketMatch match = new BracketMatch(league, round, matchNumber);
            if (roundNumber == size) {
                match.placeTeams(placements.get(team1PositionOf(matchNumber)),
                        placements.get(team2PositionOf(matchNumber)));
            }
            matches.add(match);
        }
        return matches;
    }

    public static Bracket from(final List<BracketMatch> matches) {
        List<BracketMatch> treeMatches = matches.stream()
                .filter(match -> match.getRound().isInBracketTree())
                .toList();
        if (treeMatches.isEmpty()) {
            throw new BadRequestException(BracketErrorMessages.BRACKET_NOT_FOUND);
        }
        Map<Integer, Map<Integer, BracketMatch>> matchesByRound = groupByRound(treeMatches);
        int size = matchesByRound.keySet().stream().mapToInt(Integer::intValue).max().orElseThrow();
        BracketMatch thirdPlaceMatch = matches.stream()
                .filter(match -> match.getRound() == Round.THIRD_PLACE_MATCH)
                .findAny()
                .orElse(null);
        return new Bracket(size, matchesByRound, firstRoundPlacements(matchesByRound.get(size).values()),
                thirdPlaceMatch);
    }

    private static Map<Integer, Map<Integer, BracketMatch>> groupByRound(final List<BracketMatch> matches) {
        Map<Integer, Map<Integer, BracketMatch>> matchesByRound = new HashMap<>();
        for (BracketMatch match : matches) {
            matchesByRound.computeIfAbsent(match.getRound().getNumber(), key -> new HashMap<>())
                    .put(match.getMatchNumber(), match);
        }
        return matchesByRound;
    }

    private static Map<Integer, Team> firstRoundPlacements(final Collection<BracketMatch> firstRoundMatches) {
        Map<Integer, Team> placements = new HashMap<>();
        for (BracketMatch match : firstRoundMatches) {
            if (match.getTeam1() != null) {
                placements.put(team1PositionOf(match.getMatchNumber()), match.getTeam1());
            }
            if (match.getTeam2() != null) {
                placements.put(team2PositionOf(match.getMatchNumber()), match.getTeam2());
            }
        }
        return placements;
    }

    private static int team1PositionOf(final int matchNumber) {
        return matchNumber * TEAMS_PER_MATCH - 1;
    }

    private static int team2PositionOf(final int matchNumber) {
        return matchNumber * TEAMS_PER_MATCH;
    }

    public int getSize() {
        return size;
    }

    public BracketMatch getThirdPlaceMatch() {
        return thirdPlaceMatch;
    }

    public List<Integer> roundNumbers() {
        List<Integer> roundNumbers = new ArrayList<>();
        for (int roundNumber = size; roundNumber >= Round.FINAL.getNumber(); roundNumber /= TEAMS_PER_MATCH) {
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

    public Team slotOf(final BracketMatch match, final int side) {
        if (isFirstRound(match)) {
            return side == TEAM1_SIDE ? match.getTeam1() : match.getTeam2();
        }
        BracketMatch feeder = feederOf(match, side);
        if (feeder == null) {
            return null;
        }
        return advancerOf(feeder);
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
        Team slot1 = slotOf(match, TEAM1_SIDE);
        Team slot2 = slotOf(match, TEAM2_SIDE);
        if (slot1 != null && slot2 == null && !subtreeHasTeam(match, TEAM2_SIDE)) {
            return slot1;
        }
        if (slot2 != null && slot1 == null && !subtreeHasTeam(match, TEAM1_SIDE)) {
            return slot2;
        }
        return null;
    }

    public Team winnerOf(final BracketMatch match) {
        return teamWithResult(match, GameResult.WIN);
    }

    /**
     * 3·4위전 참가팀이 준결승 패자와 일치하는지 검증한다.
     * 준결승이 끝나지 않았거나 무승부로 남아 패자가 가려지지 않으면 검증을 건너뛰어 수동 선택을 허용한다.
     */
    public void validateThirdPlaceContenders(final Long teamId1, final Long teamId2) {
        Team loser1 = thirdPlaceSlotOf(TEAM1_SIDE);
        Team loser2 = thirdPlaceSlotOf(TEAM2_SIDE);
        if (loser1 == null || loser2 == null) {
            return;
        }
        if (!Set.of(loser1.getId(), loser2.getId()).equals(Set.copyOf(List.of(teamId1, teamId2)))) {
            throw new BadRequestException(BracketErrorMessages.THIRD_PLACE_TEAMS_MISMATCH);
        }
    }

    /**
     * 3·4위전에 배정될 팀. 준결승 패자이며, 준결승이 끝나지 않았거나 무승부로 남아 패자가 없으면 null.
     */
    public Team thirdPlaceSlotOf(final int side) {
        Map<Integer, BracketMatch> semiFinals = matchesByRound.get(Round.SEMI_FINAL.getNumber());
        if (semiFinals == null) {
            return null;
        }
        BracketMatch semiFinal = semiFinals.get(side == TEAM1_SIDE ? FIRST_SEMI_FINAL : SECOND_SEMI_FINAL);
        if (semiFinal == null) {
            return null;
        }
        return teamWithResult(semiFinal, GameResult.LOSE);
    }

    private Team teamWithResult(final BracketMatch match, final GameResult result) {
        Game game = match.getGame();
        if (game == null || game.getState() != GameState.FINISHED) {
            return null;
        }
        return game.getGameTeams().stream()
                .filter(gameTeam -> gameTeam.getResult() == result)
                .map(GameTeam::getTeam)
                .findAny()
                .orElse(null);
    }

    private boolean isFirstRound(final BracketMatch match) {
        return match.getRound().getNumber() == size;
    }

    private BracketMatch feederOf(final BracketMatch match, final int side) {
        int feederRoundNumber = match.getRound().getNumber() * TEAMS_PER_MATCH;
        int feederMatchNumber = match.getMatchNumber() * TEAMS_PER_MATCH - (side == TEAM1_SIDE ? 1 : 0);
        return matchesByRound.getOrDefault(feederRoundNumber, Map.of()).get(feederMatchNumber);
    }

    // 해당 사이드로 이어지는 1라운드 구간에 배치된 팀이 하나라도 있는지 (부전승 판단용)
    private boolean subtreeHasTeam(final BracketMatch match, final int side) {
        int blockSize = size / match.getRound().getNumber();
        int start = (match.getMatchNumber() - 1) * blockSize * TEAMS_PER_MATCH + (side - 1) * blockSize + 1;
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
        return ceilDiv(position * roundNumber, size * TEAMS_PER_MATCH);
    }

    // 매치 내 좌우 슬롯을 구분하는 전역 블록 번호 (같은 값이면 같은 사이드)
    private int sideBlockOf(final int roundNumber, final int position) {
        return ceilDiv(position * roundNumber, size);
    }

    private static int ceilDiv(final int dividend, final int divisor) {
        return (dividend + divisor - 1) / divisor;
    }
}
