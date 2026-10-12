package com.sports.server.command.nl.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sports.server.command.nl.application.extract.DetectedFileType;
import com.sports.server.command.nl.application.extract.PdfTextExtractor;
import com.sports.server.command.nl.infra.NlTranscriptionClient;
import org.junit.jupiter.api.Test;

class NlRosterTranscriberTest {

    private final NlTranscriptionClient client = mock(NlTranscriptionClient.class);
    private final PdfTextExtractor pdfTextExtractor = mock(PdfTextExtractor.class);
    private final NlRosterTranscriber transcriber = new NlRosterTranscriber(client, pdfTextExtractor);

    @Test
    void 같은_파일을_두_번_올리면_AI는_한_번만_부른다() {
        when(client.transcribe(any(), any())).thenReturn("홍길동\t202312345");

        NlRosterTranscriber.Transcription first = transcriber.transcribe(new byte[]{1, 2, 3}, DetectedFileType.PNG);
        NlRosterTranscriber.Transcription second = transcriber.transcribe(new byte[]{1, 2, 3}, DetectedFileType.PNG);

        assertThat(second).isEqualTo(first);
        verify(client, times(1)).transcribe(any(), any());
    }

    @Test
    void 다른_파일은_각각_부른다() {
        when(client.transcribe(any(), any())).thenReturn("x");

        transcriber.transcribe(new byte[]{1}, DetectedFileType.PNG);
        transcriber.transcribe(new byte[]{2}, DetectedFileType.PNG);

        verify(client, times(2)).transcribe(any(), any());
    }

    @Test
    void 빈_결과는_기억하지_않는다() {
        when(client.transcribe(any(), any())).thenReturn("");

        transcriber.transcribe(new byte[]{1}, DetectedFileType.JPEG);
        transcriber.transcribe(new byte[]{1}, DetectedFileType.JPEG);

        verify(client, times(2)).transcribe(any(), any());
    }

    @Test
    void 스캔_PDF는_앞_다섯_페이지_사본을_보내고_잘렸음을_알린다() {
        byte[] original = {1, 2, 3};
        byte[] trimmed = {9};
        when(pdfTextExtractor.firstPages(original)).thenReturn(new PdfTextExtractor.TrimmedPdf(trimmed, true));
        when(client.transcribe(trimmed, "application/pdf")).thenReturn("홍길동\t202312345");

        NlRosterTranscriber.Transcription result = transcriber.transcribe(original, DetectedFileType.PDF);

        assertThat(result).isEqualTo(new NlRosterTranscriber.Transcription("홍길동\t202312345", true));
    }
}
