package com.sports.server.command.bracket.domain;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 싱글 엘리미네이션 트리에 속한 매치들을 (라운드 번호, 매치 번호) 좌표로 보관한다.
 * 트리 밖 라운드(3·4위전)는 여기에 담기지 않는다.
 */
public class BracketTree {

    private final int size;
    private final Map<Integer, Map<Integer, BracketMatch>> matchesByRound;

    private BracketTree(final int size, final Map<Integer, Map<Integer, BracketMatch>> matchesByRound) {
        this.size = size;
        this.matchesByRound = matchesByRound;
    }

    public static BracketTree from(final List<BracketMatch> treeMatches) {
        Map<Integer, Map<Integer, BracketMatch>> matchesByRound = new HashMap<>();
        for (BracketMatch match : treeMatches) {
            matchesByRound.computeIfAbsent(match.getRound().getNumber(), key -> new HashMap<>())
                    .put(match.getMatchNumber(), match);
        }
        int size = matchesByRound.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow();
        return new BracketTree(size, matchesByRound);
    }

    /**
     * 1라운드의 팀 슬롯 수. 가장 큰 라운드 번호가 곧 대진표 크기다.
     */
    public int size() {
        return size;
    }

    public boolean hasRound(final int roundNumber) {
        return matchesByRound.containsKey(roundNumber);
    }

    /**
     * 해당 좌표의 매치. 없으면 null.
     */
    public BracketMatch matchAt(final int roundNumber, final int matchNumber) {
        return matchesByRound.getOrDefault(roundNumber, Map.of()).get(matchNumber);
    }

    public List<BracketMatch> matchesOf(final int roundNumber) {
        return matchesByRound.getOrDefault(roundNumber, Map.of()).values().stream()
                .sorted(Comparator.comparingInt(BracketMatch::getMatchNumber))
                .toList();
    }

    public Collection<BracketMatch> firstRoundMatches() {
        return matchesByRound.get(size).values();
    }
}
