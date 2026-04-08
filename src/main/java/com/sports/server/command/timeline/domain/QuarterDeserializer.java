package com.sports.server.command.timeline.domain;

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
        return Quarter.resolve(p.getText());
    }
}
