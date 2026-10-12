package com.sports.server.command.nl.application.extract;

import static com.sports.server.command.nl.exception.NlErrorMessages.EXTRACT_NOTHING_READ;

import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdfTextExtractor {

    public static final int MAX_PAGES = 5;

    // 명단에는 반드시 학번이 있으므로, 6자리 이상 연속 숫자가 없으면 글자 레이어가 없는 스캔본으로 본다
    private static final Pattern DIGIT_RUN = Pattern.compile("\\d{6,}");

    private final long maxMemoryBytes;

    public PdfTextExtractor(@Value("${nl.extract.pdf-max-memory-bytes:52428800}") long maxMemoryBytes) {
        this.maxMemoryBytes = maxMemoryBytes;
    }

    public Optional<RosterText> extractText(byte[] data) {
        try (PDDocument document = load(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setEndPage(MAX_PAGES);
            String text = stripper.getText(document);
            if (!DIGIT_RUN.matcher(text).find()) {
                return Optional.empty();
            }
            RosterText roster = RosterText.ofText(text);
            if (hasMorePagesThanLimit(document)) {
                roster.markTruncated();
            }
            return Optional.of(roster);
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }
    }

    public TrimmedPdf firstPages(byte[] data) {
        try (PDDocument document = load(data); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean trimmed = hasMorePagesThanLimit(document);
            while (document.getNumberOfPages() > MAX_PAGES) {
                document.removePage(MAX_PAGES);
            }
            document.save(out);
            return new TrimmedPdf(out.toByteArray(), trimmed);
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException(EXTRACT_NOTHING_READ);
        }
    }

    private PDDocument load(byte[] data) throws IOException {
        return Loader.loadPDF(data, "", null, null, MemoryUsageSetting.setupMainMemoryOnly(maxMemoryBytes).streamCache);
    }

    private boolean hasMorePagesThanLimit(PDDocument document) {
        return document.getNumberOfPages() > MAX_PAGES;
    }

    public record TrimmedPdf(byte[] data, boolean trimmed) {
    }
}
