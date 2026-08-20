package com.sports.server.command.team.domain;

import com.sports.server.command.league.domain.SportType;
import lombok.Getter;

/**
 * 팀에서의 선수 포지션. 선수 개인이 아니라 팀 소속 정보라 {@link TeamPlayer} 가 들고 있고,
 * 경기 라인업에는 등번호와 같은 방식으로 복사된다.
 *
 * <p>displayOrder 는 관객 라인업 노출 순서다. 기획상 축구는 대분류 FW → MF → DF → GK,
 * 농구는 PG → SG → SF → PF → C 순이다.
 */
@Getter
public enum Position {

    LW(SportType.SOCCER, 1),    // FW
    ST(SportType.SOCCER, 2),
    RW(SportType.SOCCER, 3),
    LM(SportType.SOCCER, 4),    // MF
    CM(SportType.SOCCER, 5),
    RM(SportType.SOCCER, 6),
    LB(SportType.SOCCER, 7),    // DF
    CB(SportType.SOCCER, 8),
    RB(SportType.SOCCER, 9),
    GK(SportType.SOCCER, 10),   // GK

    PG(SportType.BASKETBALL, 1),
    SG(SportType.BASKETBALL, 2),
    SF(SportType.BASKETBALL, 3),
    PF(SportType.BASKETBALL, 4),
    C(SportType.BASKETBALL, 5);

    private final SportType sportType;
    private final int displayOrder;

    Position(SportType sportType, int displayOrder) {
        this.sportType = sportType;
        this.displayOrder = displayOrder;
    }

    public boolean isFor(SportType sportType) {
        return this.sportType == sportType;
    }
}
