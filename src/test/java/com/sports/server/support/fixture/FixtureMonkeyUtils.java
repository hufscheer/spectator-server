package com.sports.server.support.fixture;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

import java.util.Random;

public class FixtureMonkeyUtils {
    private static final FixtureMonkey INSTANCE = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    private static final Random RANDOM = new Random();

    public static <T> ArbitraryBuilder<T> entityBuilder(Class<T> clazz) {
        return INSTANCE.giveMeBuilder(clazz)
                .set("id", RANDOM.nextLong(1, 10000));
    }
}
