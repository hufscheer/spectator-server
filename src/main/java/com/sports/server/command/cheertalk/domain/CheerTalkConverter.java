package com.sports.server.command.cheertalk.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = false)
public class CheerTalkConverter implements AttributeConverter<CheerTalk, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CheerTalk attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("[CheerTalkConverter] 객체 -> JSON 변환 실패: {}", e.getMessage(), e);
            throw new IllegalArgumentException("CheerTalk to JSON convert error", e);
        }
    }

    @Override
    public CheerTalk convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(dbData, CheerTalk.class);
        } catch (Exception e) {
            log.error("[CheerTalkConverter] JSON -> 객체 변환 실패: {}", e.getMessage(), e);
            throw new IllegalArgumentException("JSON to CheerTalk convert error", e);
        }
    }
}

