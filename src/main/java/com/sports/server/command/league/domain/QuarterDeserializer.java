package com.sports.server.command.league.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class QuarterDeserializer extends StdDeserializer<Quarter> {

    public QuarterDeserializer() {
        super(Quarter.class);
    }

    @Override
    public Quarter deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return QuarterResolver.resolve(p.getText());
    }
}
