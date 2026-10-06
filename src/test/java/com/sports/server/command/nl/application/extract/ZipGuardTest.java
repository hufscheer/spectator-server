package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class ZipGuardTest {

    private static byte[] zip(int entries, int bytesPerEntry) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (int i = 0; i < entries; i++) {
                zip.putNextEntry(new ZipEntry("e" + i + ".txt"));
                zip.write(new byte[bytesPerEntry]);
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    @Test
    void 한도_안이면_항목_이름을_돌려준다() throws IOException {
        assertThat(ZipGuard.scan(zip(3, 100))).containsExactlyInAnyOrder("e0.txt", "e1.txt", "e2.txt");
    }

    @Test
    void 압축_해제_크기_합이_한도를_넘으면_거절한다() throws IOException {
        byte[] bomb = zip(1, 60 * 1024 * 1024); // 0 으로 채우면 수십 KB 로 압축된다
        assertThat(bomb.length).isLessThan(1024 * 1024);

        assertThatThrownBy(() -> ZipGuard.scan(bomb))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
    }

    @Test
    void 항목_수가_한도를_넘으면_거절한다() throws IOException {
        byte[] many = zip(1001, 1);

        assertThatThrownBy(() -> ZipGuard.scan(many)).isInstanceOf(BadRequestException.class);
        assertThat(ZipGuard.scan(zip(1000, 1))).hasSize(1000);
    }
}
