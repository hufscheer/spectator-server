package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor(50L * 1024 * 1024);

    private static byte[] pdf(String... lines) throws IOException {
        return pdfWithPages(1, lines);
    }

    private static byte[] pdfWithPages(int pages, String... lines) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int p = 1; p <= pages; p++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.setLeading(16f);
                    content.newLineAtOffset(50, 700);
                    for (String line : lines) {
                        content.showText(line.replace("#", String.valueOf(p)));
                        content.newLine();
                    }
                    content.endText();
                }
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static int pageCount(byte[] data) throws IOException {
        try (PDDocument doc = Loader.loadPDF(data)) {
            return doc.getNumberOfPages();
        }
    }

    @Test
    void 텍스트_레이어의_명단을_줄_단위로_읽는다() throws IOException {
        RosterText result = extractor.extractText(pdf("Name ID Number", "Hong 202312345 10", "Kim 202312346 7")).orElseThrow();

        assertThat(result.text().split("\n")).containsExactly("Name ID Number", "Hong 202312345 10", "Kim 202312346 7");
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void 학번_같은_긴_숫자가_없으면_스캔본으로_본다() throws IOException {
        assertThat(extractor.extractText(pdf("Hello world", "page 1 of 12345"))).isEmpty();
        assertThat(extractor.extractText(pdf())).isEmpty();
    }

    @Test
    void 앞_다섯_페이지만_읽고_더_있으면_truncated() throws IOException {
        RosterText result = extractor.extractText(pdfWithPages(7, "Hong 20231234# 10")).orElseThrow();

        assertThat(result.text().split("\n")).containsExactly(
                "Hong 202312341 10", "Hong 202312342 10", "Hong 202312343 10", "Hong 202312344 10", "Hong 202312345 10");
        assertThat(result.truncated()).isTrue();
    }

    @Test
    void 정확히_다섯_페이지면_truncated_아니다() throws IOException {
        assertThat(extractor.extractText(pdfWithPages(5, "Hong 20231234# 10")).orElseThrow().truncated()).isFalse();
    }

    @Test
    void 스캔본은_앞_다섯_페이지만_잘라_AI용_사본을_만든다() throws IOException {
        PdfTextExtractor.TrimmedPdf trimmed = extractor.firstPages(pdfWithPages(7, "x"));

        assertThat(pageCount(trimmed.data())).isEqualTo(5);
        assertThat(trimmed.trimmed()).isTrue();
        assertThat(extractor.firstPages(pdfWithPages(3, "x")).trimmed()).isFalse();
    }

    @Test
    void 깨진_PDF는_읽지_못했다는_400() {
        assertThatThrownBy(() -> extractor.extractText("%PDF-1.4 garbage".getBytes()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
        assertThatThrownBy(() -> extractor.firstPages("%PDF-1.4 garbage".getBytes()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 메모리_한도를_넘으면_읽지_못했다는_400() throws IOException {
        PdfTextExtractor tiny = new PdfTextExtractor(1);

        assertThatThrownBy(() -> tiny.firstPages(pdfWithPages(3, "Hong 202312345 10")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
    }
}
