package com.sports.server.command.timeline.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class QuarterConverter implements AttributeConverter<Quarter, String> {

    @Override
    public String convertToDatabaseColumn(Quarter quarter) {
        if (quarter == null) {
            return null;
        }
        return quarter.name();
    }

    @Override
    public Quarter convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Quarter.resolve(dbData);
    }
}
