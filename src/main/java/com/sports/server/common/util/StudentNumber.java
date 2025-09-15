package com.sports.server.common.util;

import com.sports.server.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class StudentNumber {
    
    private static final int ADMISSION_YEAR_START_INDEX = 2;
    private static final int ADMISSION_YEAR_END_INDEX = 4;
    private static final String STUDENT_NUMBER_PATTERN = "^[0-9]{9}$";
    
    private StudentNumber() {}

    public static boolean isInvalid(String studentNumber) {
        return studentNumber == null || !studentNumber.matches(STUDENT_NUMBER_PATTERN);
    }

    public static void validate(String studentNumber) {
        if (isInvalid(studentNumber)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "학생번호는 9자리 숫자여야 합니다.");
        }
    }

    public static String extractAdmissionYear(String studentNumber) {
        if (isInvalid(studentNumber)) return null;
        return studentNumber.substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX);
    }
}
