package com.sports.server.command.nl.application.extract;

import com.sports.server.command.nl.dto.NlSourceType;

public enum DetectedFileType {
    PNG(NlSourceType.IMAGE, "image/png"),
    JPEG(NlSourceType.IMAGE, "image/jpeg"),
    WEBP(NlSourceType.IMAGE, "image/webp"),
    HEIC(NlSourceType.IMAGE, "image/heic"),
    HEIF(NlSourceType.IMAGE, "image/heif"),
    PDF(NlSourceType.PDF, "application/pdf"),
    XLSX(NlSourceType.SPREADSHEET, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV(NlSourceType.CSV, "text/csv");

    private final NlSourceType sourceType;
    private final String mimeType;

    DetectedFileType(NlSourceType sourceType, String mimeType) {
        this.sourceType = sourceType;
        this.mimeType = mimeType;
    }

    public NlSourceType sourceType() {
        return sourceType;
    }

    public String mimeType() {
        return mimeType;
    }
}
