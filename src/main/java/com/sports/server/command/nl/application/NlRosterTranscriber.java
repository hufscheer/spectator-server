package com.sports.server.command.nl.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sports.server.command.nl.application.extract.DetectedFileType;
import com.sports.server.command.nl.application.extract.PdfTextExtractor;
import com.sports.server.command.nl.infra.NlTranscriptionClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

// 같은 파일을 다시 올릴 때 AI 비용을 또 내지 않도록 결과를 잠깐 기억한다. 키는 파일 해시이고 파일 자체는 저장하지 않는다
@Component
public class NlRosterTranscriber {

    private static final long CACHE_MAX_SIZE = 200L;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final NlTranscriptionClient client;
    private final PdfTextExtractor pdfTextExtractor;
    private final Cache<String, Transcription> cache = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterWrite(CACHE_TTL)
            .build();

    public NlRosterTranscriber(NlTranscriptionClient client, PdfTextExtractor pdfTextExtractor) {
        this.client = client;
        this.pdfTextExtractor = pdfTextExtractor;
    }

    public Transcription transcribe(byte[] data, DetectedFileType type) {
        String key = sha256(data);
        Transcription cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        Transcription fresh = callModel(data, type);
        if (!fresh.text().isBlank()) {
            cache.put(key, fresh);
        }
        return fresh;
    }

    private Transcription callModel(byte[] data, DetectedFileType type) {
        if (type == DetectedFileType.PDF) {
            PdfTextExtractor.TrimmedPdf pdf = pdfTextExtractor.firstPages(data);
            return new Transcription(client.transcribe(pdf.data(), type.mimeType()), pdf.trimmed());
        }
        return new Transcription(client.transcribe(data, type.mimeType()), false);
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record Transcription(String text, boolean sourceTrimmed) {
    }
}
