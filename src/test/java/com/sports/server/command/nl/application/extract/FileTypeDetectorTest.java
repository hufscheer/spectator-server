package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class FileTypeDetectorTest {

    private final FileTypeDetector detector = new FileTypeDetector();

    private static byte[] bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            b[i] = (byte) values[i];
        }
        return b;
    }

    private static byte[] ftyp(String brand) {
        byte[] b = new byte[24];
        b[3] = 24;
        System.arraycopy("ftyp".getBytes(StandardCharsets.US_ASCII), 0, b, 4, 4);
        System.arraycopy(brand.getBytes(StandardCharsets.US_ASCII), 0, b, 8, 4);
        return b;
    }

    private static byte[] zipWith(String entryName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(1);
            zip.closeEntry();
        }
        return out.toByteArray();
    }

    @Test
    void 이미지_매직넘버를_구분한다() {
        assertThat(detector.detect(bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0))).isEqualTo(DetectedFileType.PNG);
        assertThat(detector.detect(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0))).isEqualTo(DetectedFileType.JPEG);
        assertThat(detector.detect("RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1))).isEqualTo(DetectedFileType.WEBP);
    }

    @Test
    void RIFF지만_WEBP가_아니면_이미지로_보지_않는다() {
        assertThatThrownBy(() -> detector.detect(bytes('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E', 0)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void HEIC_HEIF_브랜드를_구분한다() {
        for (String brand : new String[]{"heic", "heix", "hevc", "heim", "heis"}) {
            assertThat(detector.detect(ftyp(brand))).as(brand).isEqualTo(DetectedFileType.HEIC);
        }
        assertThat(detector.detect(ftyp("mif1"))).isEqualTo(DetectedFileType.HEIF);
        assertThat(detector.detect(ftyp("msf1"))).isEqualTo(DetectedFileType.HEIF);
        assertThatThrownBy(() -> detector.detect(ftyp("mp42"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void PDF를_구분한다() {
        assertThat(detector.detect("%PDF-1.7\n".getBytes(StandardCharsets.US_ASCII))).isEqualTo(DetectedFileType.PDF);
    }

    @Test
    void workbook_항목이_있는_zip만_xlsx로_본다() throws IOException {
        assertThat(detector.detect(zipWith("xl/workbook.xml"))).isEqualTo(DetectedFileType.XLSX);
        assertThatThrownBy(() -> detector.detect(zipWith("word/document.xml")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사진(JPG·PNG·WEBP·HEIC), 엑셀(xlsx), CSV, PDF 만 올릴 수 있습니다.");
    }

    @Test
    void 구형_xls는_xlsx로_저장하라고_안내한다() {
        assertThatThrownBy(() -> detector.detect(bytes(0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("엑셀 파일은 xlsx 로 저장해서 올려 주세요.");
    }

    @Test
    void 텍스트는_CSV로_본다() {
        assertThat(detector.detect("이름,학번\n홍길동,202312345".getBytes(StandardCharsets.UTF_8))).isEqualTo(DetectedFileType.CSV);
    }

    @Test
    void 알_수_없는_바이트는_지원하지_않는_형식이다() {
        byte[] random = bytes(0x01, 0x02, 0x00, 0x9F, 0x13, 0x77, 0x00, 0x00);
        assertThatThrownBy(() -> detector.detect(random))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사진(JPG·PNG·WEBP·HEIC), 엑셀(xlsx), CSV, PDF 만 올릴 수 있습니다.");
    }
}
