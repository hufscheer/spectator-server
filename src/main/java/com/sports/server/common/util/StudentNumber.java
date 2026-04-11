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

    public static boolean isInvalid(String studentNumber, int digits) {
        String pattern = "^[0-9]{" + digits + "}$";
        return studentNumber == null || !studentNumber.matches(pattern);
    }

    public static void validate(String studentNumber, int digits) {
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
