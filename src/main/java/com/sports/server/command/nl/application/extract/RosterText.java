package com.sports.server.command.nl.application.extract;

import com.sports.server.command.nl.dto.NlMessageLimit;
import java.util.ArrayList;
import java.util.List;

public final class RosterText {

    public static final int MAX_LINES = 500;

    private static final String LINE_SEPARATOR = "\n";
    private static final String CELL_SEPARATOR = "\t";

    private final List<String> lines = new ArrayList<>();
    private int totalLength = 0;
    private boolean truncated = false;

    public static RosterText ofText(String text) {
        RosterText result = new RosterText();
        for (String line : text.split("\\R")) {
            result.addLine(line.stripTrailing());
        }
        return result;
    }

    public void addRow(List<String> cells) {
        List<String> cleaned = new ArrayList<>();
        for (String cell : cells) {
            cleaned.add(cell == null ? "" : cell.replaceAll("[\\t\\r\\n]+", " ").strip());
        }
        int end = cleaned.size();
        while (end > 0 && cleaned.get(end - 1).isEmpty()) {
            end--;
        }
        addLine(String.join(CELL_SEPARATOR, cleaned.subList(0, end)));
    }

    public void addLine(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        if (exceedsLimit(line)) {
            truncated = true;
            return;
        }
        totalLength += lengthWithSeparator(line);
        lines.add(line);
    }

    public void markTruncated() {
        truncated = true;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public boolean truncated() {
        return truncated;
    }

    public String text() {
        return String.join(LINE_SEPARATOR, lines);
    }

    private boolean exceedsLimit(String line) {
        return lines.size() >= MAX_LINES || totalLength + lengthWithSeparator(line) > NlMessageLimit.MAX_LENGTH;
    }

    private int lengthWithSeparator(String line) {
        return lines.isEmpty() ? line.length() : LINE_SEPARATOR.length() + line.length();
    }
}
