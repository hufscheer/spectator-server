package com.sports.server.common.advice;

import com.sports.server.command.player.dto.PlayerConflictResponse;
import com.sports.server.command.player.exception.PlayerStudentNumberConflictException;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionAdvice {

    private final AlertService alertService;

    @ExceptionHandler(PlayerStudentNumberConflictException.class)
    protected ResponseEntity<PlayerConflictResponse> handlePlayerStudentNumberConflict(
            PlayerStudentNumberConflictException e, HttpServletRequest request) {
        logClientError(request, e.getStatus(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(new PlayerConflictResponse(e.getMessage(), e.getExistingPlayer()));
    }

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

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.UNAUTHORIZED, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("인증이 필요합니다."));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        alertService.sendErrorAlert(request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getParameterName() + " 파라미터가 필요합니다."));
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("지원하지 않는 HTTP 메서드입니다."));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNotFoundEndpointException(NoHandlerFoundException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("요청한 엔드포인트를 찾을 수 없습니다."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(e.getName() + " 파라미터의 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("요청 본문을 읽을 수 없습니다."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ErrorResponse.of("지원하지 않는 Content-Type입니다."));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    protected ResponseEntity<Void> handleMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e, HttpServletRequest request) {
        logClientError(request, HttpStatus.NOT_ACCEPTABLE, e.getMessage());
        // Accept 협상이 실패한 요청이라 본문을 실으면 응답 변환이 다시 실패한다 — 상태 코드만 반환
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    private void logClientError(HttpServletRequest request, HttpStatus status, String message) {
        log.warn("[{} {}] {} {}: {}", request.getMethod(), request.getRequestURI(),
                status.value(), status.getReasonPhrase(), message);
    }

    private String formatBindingResult(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(fe -> String.format("%s: %s (rejected value: %s)",
                        fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.joining(", "));
    }
}
