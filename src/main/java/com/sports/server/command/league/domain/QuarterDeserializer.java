package com.sports.server.command.league.domain;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;


public class QuarterDeserializer extends StdDeserializer<Quarter> {

    public QuarterDeserializer() {
        super(Quarter.class);
    }

    @Override
    public Quarter deserialize(JsonParser p, DeserializationContext ctx) {
        return QuarterResolver.resolve(p.getString());
    }
}
