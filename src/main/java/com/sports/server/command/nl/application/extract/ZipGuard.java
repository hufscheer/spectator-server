package com.sports.server.command.nl.application.extract;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_NOTHING_READ;

import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// 작게 압축된 xlsx 가 수 GB 로 풀리는 zip 폭탄을 파싱 전에 막는다

final class ZipGuard {

    private static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 50L * 1024 * 1024;
    private static final int MAX_ENTRIES = 1000;

    private ZipGuard() {
    }

    static Set<String> scan(byte[] data) {
        Set<String> names = new HashSet<>();
        long total = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (names.size() >= MAX_ENTRIES) {
                    throw new BadRequestException(EXTRACT_NOTHING_READ);
                }
                names.add(entry.getName());
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        throw new BadRequestException(EXTRACT_NOTHING_READ);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }
        return names;
    }
}
