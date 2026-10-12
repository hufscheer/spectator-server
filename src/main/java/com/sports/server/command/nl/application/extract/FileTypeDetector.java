package com.sports.server.command.nl.application.extract;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_LEGACY_XLS;
import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_UNSUPPORTED_TYPE;

import com.sports.server.common.exception.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.stereotype.Component;

// 파일명·Content-Type 은 클라이언트가 마음대로 정하므로 믿지 않고 앞부분 바이트(magic bytes)로만 가른다
@Component
public class FileTypeDetector {

    private static final Set<String> HEIC_BRANDS = Set.of("heic", "heix", "hevc", "heim", "heis");
    private static final Set<String> HEIF_BRANDS = Set.of("mif1", "msf1");

    public DetectedFileType detect(byte[] d) {
        if (startsWith(d, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return DetectedFileType.PNG;
        }
        if (startsWith(d, 0xFF, 0xD8, 0xFF)) {
            return DetectedFileType.JPEG;
        }
        if (d.length >= 12 && startsWith(d, 'R', 'I', 'F', 'F') && ascii(d, 8, 4).equals("WEBP")) {
            return DetectedFileType.WEBP;
        }
        if (d.length >= 12 && ascii(d, 4, 4).equals("ftyp")) {
            String brand = ascii(d, 8, 4);
            if (HEIC_BRANDS.contains(brand)) {
                return DetectedFileType.HEIC;
            }
            if (HEIF_BRANDS.contains(brand)) {
                return DetectedFileType.HEIF;
            }
        }
        if (startsWith(d, '%', 'P', 'D', 'F', '-')) {
            return DetectedFileType.PDF;
        }
        if (startsWith(d, 0xD0, 0xCF, 0x11, 0xE0)) {
            throw new BadRequestException(EXTRACT_LEGACY_XLS);
        }
        if (startsWith(d, 'P', 'K', 0x03, 0x04)) {
            if (ZipGuard.scan(d).contains("xl/workbook.xml")) {
                return DetectedFileType.XLSX;
            }
            throw new BadRequestException(EXTRACT_UNSUPPORTED_TYPE);
        }
        if (looksLikeText(d)) {
            return DetectedFileType.CSV;
        }
        throw new BadRequestException(EXTRACT_UNSUPPORTED_TYPE);
    }

    private static boolean looksLikeText(byte[] d) {
        int limit = Math.min(d.length, 8192);
        for (int i = 0; i < limit; i++) {
            int b = d[i] & 0xFF;
            if (isBinaryControlByte(b)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBinaryControlByte(int b) {
        boolean isTextControl = b == '\t' || b == '\n' || b == '\r' || b == 0x0C;
        return (b < 0x20 && !isTextControl) || b == 0x7F;
    }

    private static boolean startsWith(byte[] d, int... prefix) {
        if (d.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((d[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String ascii(byte[] d, int offset, int length) {
        return new String(d, offset, length, StandardCharsets.ISO_8859_1);
    }
}
