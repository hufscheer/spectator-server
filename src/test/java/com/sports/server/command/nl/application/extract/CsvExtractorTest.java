package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CsvExtractorTest {

    private final CsvExtractor extractor = new CsvExtractor();

    @Test
    void BOM이_붙은_UTF8을_읽는다() {
        byte[] body = "이름,학번,등번호\r\n홍길동,202312345,10\r\n\r\n김철수,202312346,\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.writeBytes(body);

        RosterText result = extractor.extract(out.toByteArray());

        assertThat(result.text()).isEqualTo("이름\t학번\t등번호\n홍길동\t202312345\t10\n김철수\t202312346");
    }

    @Test
    void UTF8이_아니면_MS949로_읽는다() {
        byte[] data = "이름,학번\n홍길동,202312345\n".getBytes(Charset.forName("MS949"));

        assertThat(extractor.extract(data).text()).isEqualTo("이름\t학번\n홍길동\t202312345");
    }

    @Test
    void 따옴표_안의_쉼표_줄바꿈_이중따옴표를_처리한다() {
        byte[] data = "\"홍,길동\",202312345,\"A팀\n주장\"\n\"김 \"\"철수\"\"\",202312346\n".getBytes(StandardCharsets.UTF_8);

        assertThat(extractor.extract(data).text())
                .isEqualTo("홍,길동\t202312345\tA팀 주장\n김 \"철수\"\t202312346");
    }

    @Test
    void 오백줄을_넘으면_자른다() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            sb.append("p").append(i).append("\n");
        }

        RosterText result = extractor.extract(sb.toString().getBytes(StandardCharsets.UTF_8));

        assertThat(result.text().split("\n")).hasSize(500);
        assertThat(result.truncated()).isTrue();
    }
}
