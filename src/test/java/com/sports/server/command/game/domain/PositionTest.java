package com.sports.server.command.game.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.league.domain.SportType;
import java.util.Arrays;
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

    @Test
    void 농구는_대분류_입력이_존재하지_않는다() {
        assertThat(Arrays.stream(Position.values())
                .filter(position -> position.isFor(SportType.BASKETBALL))
                .noneMatch(Position::isCategoryOnly)).isTrue();
    }

    @Test
    void 축구_대분류는_FW_MF_DF_GK_순으로_정렬된다() {
        assertThat(Arrays.stream(Position.values())
                .filter(Position::isCategoryOnly)
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .toList())
                .containsExactly(Position.FW, Position.MF, Position.DF);
        assertThat(Position.GK.getDisplayOrder())
                .isGreaterThan(Position.DF.getDisplayOrder());
    }
}
