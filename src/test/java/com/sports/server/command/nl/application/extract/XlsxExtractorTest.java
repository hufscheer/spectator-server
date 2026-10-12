package com.sports.server.command.nl.application.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.common.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class XlsxExtractorTest {

    private final XlsxExtractor extractor = new XlsxExtractor();

    @Test
    void 큰_숫자_학번은_과학표기와_소수점_없이_그대로_읽는다() {
        byte[] xlsx = XlsxFixture.create()
                .sheet(List.of(
                        List.of("이름", "학번", "등번호"),
                        List.of("홍길동", "n:202312345", "n:10"),
                        List.of("김철수", "n:2.02312346E8", "n:7"),
                        List.of("이영희", "n:202312347.0")))
                .build();

        RosterText result = extractor.extract(xlsx);

        assertThat(result.text()).isEqualTo(
                "이름\t학번\t등번호\n홍길동\t202312345\t10\n김철수\t202312346\t7\n이영희\t202312347");
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void 모든_시트를_순서대로_읽고_빈_행과_뒤쪽_빈_칸은_버린다() {
        byte[] xlsx = XlsxFixture.create()
                .sheet(List.of(
                        List.of("홍길동", "n:202312345", ""),
                        List.of(),
                        List.of("김철수", "", "n:7")))
                .sheet(List.of(List.of("박민수", "n:202312349")))
                .build();

        RosterText result = extractor.extract(xlsx);

        assertThat(result.text()).isEqualTo("홍길동\t202312345\n김철수\t\t7\n박민수\t202312349");
    }

    @Test
    void 오백줄을_넘으면_자르고_truncated_를_켠다() {
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            rows.add(List.of("p" + i));
        }

        RosterText result = extractor.extract(XlsxFixture.create().sheet(rows).build());

        assertThat(result.text().split("\n")).hasSize(500);
        assertThat(result.truncated()).isTrue();
    }

    @Test
    void 깨진_xlsx는_읽지_못했다는_400을_낸다() {
        assertThatThrownBy(() -> extractor.extract(new byte[]{'P', 'K', 3, 4, 1, 2, 3}))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("파일에서 명단을 읽지 못했습니다.");
    }
}
