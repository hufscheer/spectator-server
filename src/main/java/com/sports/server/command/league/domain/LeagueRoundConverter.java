package com.sports.server.command.league.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LeagueRoundConverter implements AttributeConverter<LeagueRound, String> {
	@Override
	public String convertToDatabaseColumn(LeagueRound attribute) {
		return attribute.getDescription();
	}

	@Override
	public LeagueRound convertToEntityAttribute(String dbData) {
		return LeagueRound.from(dbData);
	}
}
