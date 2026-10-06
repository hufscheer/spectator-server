package com.sports.server.command.nl.application.extract;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_NOTHING_READ;

import com.sports.server.common.exception.BadRequestException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvExtractor {

    // 한국어 엑셀이 CSV 를 UTF-8 이 아닌 MS949 로 저장한다
    private static final Charset MS949 = Charset.forName("MS949");

    public RosterText extract(byte[] data) {
        String content = decode(data);
        if (content.startsWith("﻿")) {
            content = content.substring(1);
        }
        char delimiter = detectDelimiter(content);
        RosterText result = new RosterText();
        for (List<String> record : parse(content, delimiter)) {
            if (result.truncated()) {
                break;
            }
            result.addRow(record);
        }
        return result;
    }

    private String decode(byte[] data) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(data))
                    .toString();
        } catch (CharacterCodingException e) {
            try {
                return MS949.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(data))
                        .toString();
            } catch (CharacterCodingException ex) {
                throw new BadRequestException(EXTRACT_NOTHING_READ);
            }
        }
    }

    private char detectDelimiter(String content) {
        int eol = content.indexOf('\n');
        String first = eol < 0 ? content : content.substring(0, eol);
        return first.indexOf(',') < 0 && first.indexOf('\t') >= 0 ? '\t' : ',';
    }

    private static List<List<String>> parse(String content, char delimiter) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int n = content.length();
        for (int i = 0; i < n; i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < n && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"' && field.length() == 0) {
                inQuotes = true;
            } else if (c == delimiter) {
                record.add(field.toString());
                field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < n && content.charAt(i + 1) == '\n') {
                    i++;
                }
                record.add(field.toString());
                field.setLength(0);
                records.add(record);
                record = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }
        return records;
    }
}
