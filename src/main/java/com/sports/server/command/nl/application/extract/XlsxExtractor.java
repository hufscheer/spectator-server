package com.sports.server.command.nl.application.extract;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_NOTHING_READ;

import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.springframework.stereotype.Component;

@Component
public class XlsxExtractor {

    private static final Pattern SCIENTIFIC = Pattern.compile("-?\\d+(\\.\\d+)?[eE][+-]?\\d+");

    public RosterText extract(byte[] data) {
        ZipGuard.scan(data);
        RosterText result = new RosterText();
        try (ReadableWorkbook workbook = new ReadableWorkbook(new ByteArrayInputStream(data))) {
            for (Sheet sheet : workbook.getSheets().toList()) {
                try (var rows = sheet.openStream()) {
                    var iterator = rows.iterator();
                    while (iterator.hasNext() && !result.truncated()) {
                        result.addRow(cellTexts(iterator.next()));
                    }
                }
                if (result.truncated()) {
                    break;
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }
        return result;
    }

    private List<String> cellTexts(Row row) {
        List<String> cells = new ArrayList<>();
        for (int i = 0; i < row.getCellCount(); i++) {
            cells.add(cellText(row.getCell(i)));
        }
        return cells;
    }

    private String cellText(Cell cell) {
        if (cell == null || cell.getType() == CellType.EMPTY || cell.getType() == CellType.ERROR) {
            return "";
        }
        String text;
        if (cell.getType() == CellType.NUMBER) {
            text = plain(cell.asNumber());
        } else {
            text = cell.getText();
            if (text != null && SCIENTIFIC.matcher(text.trim()).matches()) {
                text = plain(new BigDecimal(text.trim()));
            }
        }
        if (text == null) {
            return "";
        }
        return text;
    }

    // 학번이 과학 표기나 ".0" 으로 바뀌면 원문과 대조하는 다음 단계가 실패한다
    private static String plain(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.signum() == 0 ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
