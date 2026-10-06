package com.sports.server.support.fixture;

import com.sports.server.command.league.domain.Round;
import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

public class FixtureMonkeyUtils {
    // 기본값은 엔티티의 컬렉션마다 0~3개를 무작위로 채우고 그 안의 엔티티도 다시 채워, sample() 한 번에 1~5초가 걸렸다(CI 테스트 시간의 2/3).
    // 컬렉션은 비워서 만든다. 필요한 테스트는 set 으로 직접 채운다
    public static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(0, 0))
            .build();

    private static final Random RANDOM = new Random();

    public static <T> ArbitraryBuilder<T> entityBuilder(Class<T> clazz) {
        return INSTANCE.giveMeBuilder(clazz)
                .set("id", RANDOM.nextLong(1, 10000));
    }

    public static Arbitrary<Round> maxRoundArbitrary() {
        return Arbitraries.of(Round.class);
    }

    public static StringArbitrary nameArbitrary() {
        return Arbitraries.strings();
    }
}
