package com.sports.server.common.advice;

import com.sports.server.common.application.AlertService;
import com.sports.server.common.dto.ErrorResponse;
import com.sports.server.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionAdvice {

    private final AlertService alertService;

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("Custom 500 에러 발생: {}", e.getMessage(), e);
            alertService.sendErrorAlert(request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        } else {
            logClientError(request, e.getStatus(), e.getMessage());
        }
        return ResponseEntity.status(e.getStatus())
                .body(ErrorResponse.of(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        alertService.sendErrorAlert(request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, formatBindingResult(e.getBindingResult()));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getBindingResult()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentsNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, formatBindingResult(e.getBindingResult()));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getBindingResult()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNotFoundEndpointException(NoHandlerFoundException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("요청한 엔드포인트를 찾을 수 없습니다."));
    }

    private void logClientError(HttpServletRequest request, HttpStatus status, String message) {
        log.warn("[{} {}] {} {}: {}", request.getMethod(), request.getRequestURI(),
                status.value(), status.getReasonPhrase(), message);
    }

    private String formatBindingResult(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }
}
