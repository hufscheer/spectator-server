package com.sports.server.common.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String message;

    public static ErrorResponse createWithMessage(final String message) {
        return ErrorResponse.builder()
                .message(message)
                .build();
    }

}
