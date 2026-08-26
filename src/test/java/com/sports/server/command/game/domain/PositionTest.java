package com.sports.server.command.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sports.server.command.league.domain.SportType;
import com.sports.server.common.exception.BadRequestException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("포지션은")
class PositionTest {

    @ParameterizedTest(name = "{0} 의 대분류는 {1}")
    @CsvSource({
            "LW,FW", "ST,FW", "RW,FW", "FW,FW",
            "LM,MF", "CM,MF", "RM,MF", "MF,MF",
            "LB,DF", "CB,DF", "RB,DF", "DF,DF",
            "GK,GK"
    })
    void 축구_세부와_대분류_전용_값이_같은_대분류로_접힌다(Position position, Position expected) {
        assertThat(position.category()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} 은 대분류가 자기 자신")
    @EnumSource(value = Position.class, names = {"PG", "SG", "SF", "PF", "C"})
    void 농구는_대분류가_없어_자기_자신으로_접힌다(Position position) {
        assertThat(position.category()).isEqualTo(position);
    }

    @ParameterizedTest(name = "{0} 은 대분류까지만 입력된 값")
    @EnumSource(value = Position.class, names = {"FW", "MF", "DF"})
    void 대분류_전용_값만_대분류_입력으로_판정한다(Position position) {
        assertThat(position.isCategoryOnly()).isTrue();
    }

    @Test
    void 골키퍼는_세부가_곧_대분류라_대분류_입력으로_보지_않는다() {
        assertThat(Position.GK.isCategoryOnly()).isFalse();
    }

    @ParameterizedTest(name = "농구 포지션 {0} 은 대분류 입력이 아니다")
    @EnumSource(value = Position.class, names = {"PG", "SG", "SF", "PF", "C"})
    void 농구는_대분류_입력이_존재하지_않는다(Position position) {
        assertThat(position.isCategoryOnly()).isFalse();
    }

    @Test
    void 축구는_FW_MF_DF_GK_순으로_정렬된다() {
        List<Position> shuffled = List.of(Position.GK, Position.CB, Position.CM, Position.ST);

        assertThat(shuffled.stream().sorted(Position.DISPLAY_ORDER).toList())
                .containsExactly(Position.ST, Position.CM, Position.CB, Position.GK);
    }

    @Test
    void 농구는_PG_SG_SF_PF_C_순으로_정렬된다() {
        List<Position> shuffled = List.of(Position.C, Position.SF, Position.PG, Position.PF, Position.SG);

        assertThat(shuffled.stream().sorted(Position.DISPLAY_ORDER).toList())
                .containsExactly(Position.PG, Position.SG, Position.SF, Position.PF, Position.C);
    }

    @Test
    void 대분류는_같은_그룹의_세부와_같은_자리에_놓인다() {
        List<Position> mixed = List.of(Position.GK, Position.DF, Position.MF, Position.FW);

        assertThat(mixed.stream().sorted(Position.DISPLAY_ORDER).toList())
                .containsExactly(Position.FW, Position.MF, Position.DF, Position.GK);
    }

    @Test
    void 포지션이_없는_선수는_뒤로_밀린다() {
        List<Position> withNull = Arrays.asList(Position.GK, null, Position.ST);

        assertThat(withNull.stream().sorted(Position.DISPLAY_ORDER).toList())
                .containsExactly(Position.ST, Position.GK, null);
    }

    @Test
    void 경기_종목에_없는_포지션이면_예외가_발생한다() {
        assertAll(
                () -> assertThatThrownBy(() -> Position.PG.validateFor(SportType.SOCCER))
                        .isInstanceOf(BadRequestException.class),
                () -> assertThatThrownBy(() -> Position.GK.validateFor(SportType.BASKETBALL))
                        .isInstanceOf(BadRequestException.class),
                () -> assertThatCode(() -> Position.GK.validateFor(SportType.SOCCER))
                        .doesNotThrowAnyException()
        );
    }
}
