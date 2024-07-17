package com.sports.server.support.fixture;

import java.util.Random;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

public class FixtureMonkeyUtils {
    public static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    private static final Random RANDOM = new Random();

    public static <T> ArbitraryBuilder<T> entityBuilder(Class<T> clazz) {
        return INSTANCE.giveMeBuilder(clazz)
                .set("id", RANDOM.nextLong(1, 10000));
    }

    public static Arbitrary<Integer> maxRoundArbitrary() {
        return Arbitraries.of(4, 8, 16, 32);
    }

    public static StringArbitrary nameArbitrary() {
        return Arbitraries.strings();
    }
}
