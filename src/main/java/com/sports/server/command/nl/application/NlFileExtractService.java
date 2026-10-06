package com.sports.server.command.nl.application;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_EMPTY_FILE;
import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_NOTHING_READ;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.application.extract.CsvExtractor;
import com.sports.server.command.nl.application.extract.DetectedFileType;
import com.sports.server.command.nl.application.extract.FileTypeDetector;
import com.sports.server.command.nl.application.extract.PdfTextExtractor;
import com.sports.server.command.nl.application.extract.RosterText;
import com.sports.server.command.nl.application.extract.XlsxExtractor;
import com.sports.server.command.nl.dto.NlExtractResponse;
import com.sports.server.common.exception.BadRequestException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 파일은 저장하지 않고, 내용·추출 텍스트는 개인정보라 로그에 남기지 않는다(종류·크기·소요시간만)
@Slf4j
@Service
@RequiredArgsConstructor
public class NlFileExtractService {

    private final NlExtractRateLimiter rateLimiter;
    private final FileTypeDetector detector;
    private final XlsxExtractor xlsxExtractor;
    private final CsvExtractor csvExtractor;
    private final PdfTextExtractor pdfTextExtractor;
    private final NlRosterTranscriber transcriber;

    public NlExtractResponse extract(MultipartFile file, Member member) {
        rateLimiter.check(member.getId());
        long start = System.nanoTime();
        byte[] data = readBytes(file);
        if (data.length == 0) {
            throw new BadRequestException(EXTRACT_EMPTY_FILE);
        }

        DetectedFileType type = detector.detect(data);
        RosterText roster = switch (type) {
            case XLSX -> xlsxExtractor.extract(data);
            case CSV -> csvExtractor.extract(data);
            case PDF -> extractPdf(data);
            default -> transcribe(data, type);
        };
        if (roster.isEmpty()) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }

        log.info("NL file extract. type={}, sizeBytes={}, elapsedMs={}",
                type, data.length, (System.nanoTime() - start) / 1_000_000);
        return new NlExtractResponse(roster.text(), type.sourceType(), roster.truncated());
    }

    private RosterText extractPdf(byte[] data) {
        return pdfTextExtractor.extractText(data)
                .orElseGet(() -> transcribe(data, DetectedFileType.PDF));
    }

    private RosterText transcribe(byte[] data, DetectedFileType type) {
        NlRosterTranscriber.Transcription transcription = transcriber.transcribe(data, type);
        RosterText roster = RosterText.ofText(transcription.text());
        if (transcription.sourceTrimmed()) {
            roster.markTruncated();
        }
        return roster;
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }
    }
}
