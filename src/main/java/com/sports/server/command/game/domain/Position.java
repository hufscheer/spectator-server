package com.sports.server.command.game.domain;

import com.sports.server.command.game.exception.LineupErrorMessages;
import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import lombok.Getter;

/**
 * 경기 라인업에서의 선수 포지션. 선수 개인이나 팀의 고정 정보가 아니라 <b>그 경기의 라인업 정보</b>라
 * {@link LineupPlayer} 가 들고 있다. 같은 선수가 경기 A 에서는 LW, 경기 B 에서는 RW 일 수 있다.
 *
 * <p>축구는 세부 포지션 9종 + 골키퍼에 더해 <b>대분류 전용 값</b>({@link #FW}·{@link #MF}·{@link #DF})을 둔다.
 * 기획상 매니저가 대분류만 고르고 세부는 "선택 안 함" 으로 둘 수 있어야 하는데, 그렇게 저장된 값이
 * 세부 포지션과 같은 컬럼에 들어가기 때문이다. 골키퍼는 대분류와 세부가 같아 별도 값이 필요 없다.
 *
 * <p>농구 5종에는 대분류 개념이 없다. 그래서 농구 팀에는 대분류 전용 값이 존재하지 않고,
 * 관객 화면도 항상 세부(=그 자체) 아니면 미표시 둘 중 하나가 된다.
 *
 * <p>displayOrder 는 관객 라인업 노출 순서다. 기획상 축구는 대분류 FW → MF → DF → GK,
 * 농구는 PG → SG → SF → PF → C 순이다. 대분류 전용 값은 자기 그룹의 첫 세부 값과 같은 순서를 갖는데,
 * 대분류와 세부가 한 화면에 섞이는 경우가 없어(섞이면 전부 대분류로 낮춰 표시) 충돌하지 않는다.
 */
@Getter
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

    private final SportType sportType;
    private final int displayOrder;

    Position(SportType sportType, int displayOrder) {
        this.sportType = sportType;
        this.displayOrder = displayOrder;
    }

    public boolean isFor(SportType sportType) {
        return this.sportType == sportType;
    }

    /**
     * 경기 종목에 없는 포지션이면 거부한다. 축구 경기에 PG, 농구 경기에 GK 를 넣는 것을 막는다.
     * 미입력(null)은 허용이라 호출 측에서 걸러 넣는다.
     */
    public void validateFor(SportType sportType) {
        if (!isFor(sportType)) {
            throw new BadRequestException(
                    String.format(LineupErrorMessages.POSITION_NOT_FOR_SPORT_TYPE_EXCEPTION, name()));
        }
    }

    /**
     * 이 포지션이 속한 대분류. 축구 세부 포지션은 자기 그룹의 대분류로, 대분류 전용 값과 골키퍼는 자기 자신으로,
     * 농구는 대분류가 없어 자기 자신으로 각각 접힌다.
     */
    public Position category() {
        return switch (this) {
            case LW, ST, RW, FW -> FW;
            case LM, CM, RM, MF -> MF;
            case LB, CB, RB, DF -> DF;
            default -> this;
        };
    }

    /**
     * 대분류까지만 입력된 값인지. 세부 포지션이 존재하는데 고르지 않은 경우만 해당하므로
     * 골키퍼(세부가 곧 대분류)와 농구는 제외된다.
     */
    public boolean isCategoryOnly() {
        return this == FW || this == MF || this == DF;
    }
}
