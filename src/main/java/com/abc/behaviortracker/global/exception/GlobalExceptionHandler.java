package com.abc.behaviortracker.global.exception;

import com.abc.behaviortracker.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest req) {
        ErrorCode ec = e.getErrorCode();
        log.warn("[BusinessException] code={} {} {} - {}", ec.getCode(), req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorCode ec = ErrorCode.VALIDATION_FAILED;
        log.warn("[Validation] {} {} - {}", req.getMethod(), req.getRequestURI(), detail);
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), detail));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.INVALID_REQUEST;
        log.warn("[BadRequest] {} {} - {}: {}", req.getMethod(), req.getRequestURI(),
                e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        log.warn("[MethodNotAllowed] {} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(405)
                .body(ApiResponse.fail("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException e, HttpServletRequest req) {
        log.warn("[NotFound] {} {}", req.getMethod(), req.getRequestURI());
        return ResponseEntity.status(404)
                .body(ApiResponse.fail("NOT_FOUND", "요청한 경로를 찾을 수 없습니다"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.UNAUTHORIZED;
        log.warn("[Authentication] {} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.INVALID_CREDENTIALS;
        log.warn("[BadCredentials] {} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.FORBIDDEN_ACCESS;
        log.warn("[AccessDenied] {} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.INVALID_SESSION_STATE;
        log.warn("[IllegalState] {} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e, HttpServletRequest req) {
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("[Unexpected] {} {}", req.getMethod(), req.getRequestURI(), e);
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }
}
