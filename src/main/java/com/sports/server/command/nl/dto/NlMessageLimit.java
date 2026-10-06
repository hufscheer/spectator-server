package com.sports.server.command.nl.dto;

/** /nl/parse·/nl/process 의 message 길이 상한. 파일 추출 결과도 이 길이를 넘기지 않아야 다음 단계가 거절하지 않는다. */
public final class NlMessageLimit {

    public static final int MAX_LENGTH = 5000;

    private NlMessageLimit() {
    }
}
