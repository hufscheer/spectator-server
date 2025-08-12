package com.sports.server.support.fixture;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.sports.server.command.player.domain.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerFixture {

    private static final FixtureMonkey fixtureMonkey = FixtureMonkeyUtils.INSTANCE;

    public static Player createPlayer(Long id, String name, String studentNumber) {
        return fixtureMonkey.giveMeBuilder(Player.class)
                .set("id", id)
                .set("name", name)
                .set("studentNumber", studentNumber)
                .sample();
    }

    public static List<Player> createPlayers(int countToCreate){
        List<Player> players = new ArrayList<>();
        for (int i = 1; i < countToCreate; i++) {
            players.add(fixtureMonkey.giveMeBuilder(Player.class)
                    .set("id", (long) i)
                    .set("name", "선수" + i)
                    .set("studentNumber", "20250000" + i)
                    .sample());
        }
        return players;
    }
}
