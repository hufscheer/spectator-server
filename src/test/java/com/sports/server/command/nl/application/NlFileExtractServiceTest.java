package com.sports.server.command.nl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sports.server.command.member.domain.Member;
import com.sports.server.command.nl.application.extract.CsvExtractor;
import com.sports.server.command.nl.application.extract.DetectedFileType;
import com.sports.server.command.nl.application.extract.FileTypeDetector;
import com.sports.server.command.nl.application.extract.PdfTextExtractor;
import com.sports.server.command.nl.application.extract.XlsxExtractor;
import com.sports.server.command.nl.dto.NlExtractResponse;
import com.sports.server.command.nl.dto.NlSourceType;
import com.sports.server.command.nl.exception.NlRateLimitException;
import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class NlFileExtractServiceTest {

    private final NlExtractRateLimiter rateLimiter = mock(NlExtractRateLimiter.class);
    private final NlRosterTranscriber transcriber = mock(NlRosterTranscriber.class);
    private final NlFileExtractService service = new NlFileExtractService(
            rateLimiter, new FileTypeDetector(), new XlsxExtractor(), new CsvExtractor(),
            new PdfTextExtractor(50L * 1024 * 1024), transcriber);
    private final Member member = Member.manager("a@b.c", "pw");

    private static MockMultipartFile file(byte[] data) {
        return new MockMultipartFile("file", "x.bin", "application/octet-stream", data);
    }

    private static byte[] pdf(String line) throws IOException {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                c.beginText();
                c.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                c.newLineAtOffset(50, 700);
                if (line != null) {
                    c.showText(line);
                }
                c.endText();
            }
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void CSV는_AI_없이_탭으로_바꿔_돌려준다() {
        NlExtractResponse response = service.extract(
                file("홍길동,202312345,10\n".getBytes(StandardCharsets.UTF_8)), member);

        assertThat(response).isEqualTo(new NlExtractResponse("홍길동\t202312345\t10", NlSourceType.CSV, false));
        verify(transcriber, never()).transcribe(any(), any());
    }

    @Test
    void 이미지는_전사_클라이언트로_보낸다() {
        when(transcriber.transcribe(any(), eq(DetectedFileType.PNG)))
                .thenReturn(new NlRosterTranscriber.Transcription("홍길동\t202312345\n\n김철수\t202312346", false));
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};

        NlExtractResponse response = service.extract(file(png), member);

        assertThat(response.sourceType()).isEqualTo(NlSourceType.IMAGE);
        assertThat(response.text()).isEqualTo("홍길동\t202312345\n김철수\t202312346");
    }

    @Test
    void 텍스트_레이어가_있는_PDF는_AI를_쓰지_않는다() throws IOException {
        NlExtractResponse response = service.extract(file(pdf("Hong 202312345 10")), member);

        assertThat(response.sourceType()).isEqualTo(NlSourceType.PDF);
        assertThat(response.text()).isEqualTo("Hong 202312345 10");
        verify(transcriber, never()).transcribe(any(), any());
    }

    @Test
    void 학번이_없는_PDF는_스캔본으로_보고_전사한다() throws IOException {
        when(transcriber.transcribe(any(), eq(DetectedFileType.PDF)))
                .thenReturn(new NlRosterTranscriber.Transcription("홍길동\t202312345", true));

        NlExtractResponse response = service.extract(file(pdf(null)), member);

        assertThat(response).isEqualTo(new NlExtractResponse("홍길동\t202312345", NlSourceType.PDF, true));
    }

    @Test
    void 빈_파일은_400() {
        assertThatThrownBy(() -> service.extract(file(new byte[0]), member))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("빈 파일입니다. 내용이 있는 파일을 올려 주세요.");
    }

    @Test
    void 아무것도_읽지_못하면_400() {
        when(transcriber.transcribe(any(), any())).thenReturn(new NlRosterTranscriber.Transcription("  ", false));
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0};

        assertThatThrownBy(() -> service.extract(file(jpeg), member))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
        assertThatThrownBy(() -> service.extract(file("\n\n,,\n".getBytes(StandardCharsets.UTF_8)), member))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
    }

    @Test
    void 한도를_넘으면_파일을_읽기_전에_429() {
        org.mockito.Mockito.doThrow(new NlRateLimitException("limit")).when(rateLimiter).check(any());

        assertThatThrownBy(() -> service.extract(file(new byte[]{1}), member))
                .isInstanceOf(NlRateLimitException.class);
    }
}
