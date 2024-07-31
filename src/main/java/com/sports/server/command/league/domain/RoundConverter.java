package com.sports.server.command.league.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoundConverter implements AttributeConverter<Round, String> {
	@Override
	public String convertToDatabaseColumn(Round attribute) {
		return attribute.getDescription();
	}

	@Override
	public Round convertToEntityAttribute(String dbData) {
		return Round.from(dbData);
	}
}
