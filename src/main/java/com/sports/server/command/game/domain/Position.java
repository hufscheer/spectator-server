package com.sports.server.command.game.domain;

import com.sports.server.command.game.exception.LineupErrorMessages;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import java.util.Comparator;

/**
 * 경기 라인업에서의 선수 포지션. 팀이 아니라 그 경기의 정보라 같은 선수가 경기마다 다를 수 있다.
 *
 * <p>{@link #FW}·{@link #MF}·{@link #DF} 는 대분류 전용 값이다. 매니저가 세부를 "선택 안 함" 으로
 * 둔 경우가 세부와 같은 컬럼에 저장되기 때문에 값으로 존재한다. 골키퍼는 대분류와 세부가 같고,
 * 농구에는 대분류 개념이 없다.
 */
public enum Position {

    LW(SportType.SOCCER, 1),
    ST(SportType.SOCCER, 2),
    RW(SportType.SOCCER, 3),
    LM(SportType.SOCCER, 4),
    CM(SportType.SOCCER, 5),
    RM(SportType.SOCCER, 6),
    LB(SportType.SOCCER, 7),
    CB(SportType.SOCCER, 8),
    RB(SportType.SOCCER, 9),
    GK(SportType.SOCCER, 10),

    FW(SportType.SOCCER, 1),
    MF(SportType.SOCCER, 4),
    DF(SportType.SOCCER, 7),

    PG(SportType.BASKETBALL, 1),
    SG(SportType.BASKETBALL, 2),
    SF(SportType.BASKETBALL, 3),
    PF(SportType.BASKETBALL, 4),
    C(SportType.BASKETBALL, 5);

    /**
     * 축구 FW → MF → DF → GK, 농구 PG → SG → SF → PF → C 순. 대분류 사이의 순서는 기획이 정했고,
     * 축구 대분류 안의 세부 순서(좌 → 중앙 → 우)는 기획에 없어 표기 관례를 따랐다.
     * 대분류 전용 값은 자기 그룹의 첫 세부와 같은 자리인데, 둘이 한 화면에 섞이지 않아 충돌하지 않는다.
     */
    public static final Comparator<Position> DISPLAY_ORDER =
            Comparator.nullsLast(Comparator.comparingInt(position -> position.displayOrder));

    private final SportType sportType;
    private final int displayOrder;

    Position(SportType sportType, int displayOrder) {
        this.sportType = sportType;
        this.displayOrder = displayOrder;
    }

    private boolean isFor(SportType sportType) {
        return this.sportType == sportType;
    }

    public void validateFor(SportType sportType) {
        if (!isFor(sportType)) {
            throw new BadRequestException(
                    String.format(LineupErrorMessages.POSITION_NOT_FOR_SPORT_TYPE_EXCEPTION, name()));
        }
    }

    /** 대분류 전용 값·골키퍼·농구는 자기 자신으로 접힌다. */
    public Position category() {
        return switch (this) {
            case LW, ST, RW, FW -> FW;
            case LM, CM, RM, MF -> MF;
            case LB, CB, RB, DF -> DF;
            default -> this;
        };
    }

    /** 세부가 존재하는데 고르지 않은 경우만 해당한다. 골키퍼와 농구는 제외. */
    public boolean isCategoryOnly() {
        return this == FW || this == MF || this == DF;
    }
}
