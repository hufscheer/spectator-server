package com.sports.server.common.util;

import com.sports.server.common.exception.CustomException;
import com.sports.server.common.exception.ExceptionMessages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudentNumber {

    private static final int ADMISSION_YEAR_START_INDEX = 2;
    private static final int ADMISSION_YEAR_END_INDEX = 4;
    private static final String PERMISSIVE_PATTERN = "^[0-9]{9,10}$";

    public static boolean isInvalid(String studentNumber) {
        return studentNumber == null || !studentNumber.matches(PERMISSIVE_PATTERN);
    }

    /**
     * {@code digits} 가 {@code null} 이면 자리수를 하나로 고정하지 않고 9~10 자리를 모두 받는다.
     */
    public static boolean isInvalid(String studentNumber, Integer digits) {
        if (digits == null) {
            return isInvalid(studentNumber);
        }
        String pattern = "^[0-9]{" + digits + "}$";
        return studentNumber == null || !studentNumber.matches(pattern);
    }

    /**
     * {@code digits} 가 {@code null} 이면 자리수를 하나로 고정하지 않고 9~10 자리를 모두 받는다.
     * 여러 학교가 한 조직으로 묶이는 대회는 학교마다 학번 자리수가 달라 하나로 정할 수 없다.
     */
    public static void validate(String studentNumber, Integer digits) {
        if (digits == null) {
            if (isInvalid(studentNumber)) {
                throw new CustomException(HttpStatus.BAD_REQUEST,
                        ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID_RANGE);
            }
            return;
        }
        if (isInvalid(studentNumber, digits)) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    String.format(ExceptionMessages.PLAYER_STUDENT_NUMBER_INVALID, digits));
        }
    }

    public static String extractAdmissionYear(String studentNumber) {
        if (isInvalid(studentNumber)) return null;
        return studentNumber.substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX);
    }
}
