package com.abc.behaviortracker.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== Common =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 검증에 실패했습니다"),

    // ===== Auth (US-29) =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN_ACCESS", "접근 권한이 없습니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다"),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_FORMAT", "비밀번호 형식이 올바르지 않습니다"),

    // ===== Student (US-01~04) =====
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND", "해당 학생을 찾을 수 없습니다"),
    STUDENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "STUDENT_ALREADY_EXISTS", "이미 등록된 학생입니다"),

    // ===== Teacher =====
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEACHER_NOT_FOUND", "해당 교사를 찾을 수 없습니다"),

    // ===== Record / Session (US-05, US-08, US-23) =====
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "해당 기록 세션을 찾을 수 없습니다"),
    ACTIVE_SESSION_EXISTS(HttpStatus.CONFLICT, "ACTIVE_SESSION_EXISTS", "이미 진행 중인 기록 세션이 있습니다"),
    INVALID_SESSION_STATE(HttpStatus.CONFLICT, "INVALID_SESSION_STATE", "현재 상태에서 수행할 수 없는 작업입니다"),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD_NOT_FOUND", "해당 행동 기록을 찾을 수 없습니다"),

    // ===== Session Media (US-07) =====
    SESSION_MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_MEDIA_NOT_FOUND", "해당 미디어를 찾을 수 없습니다"),
    SESSION_MEDIA_ALREADY_EXISTS(HttpStatus.CONFLICT, "SESSION_MEDIA_ALREADY_EXISTS", "해당 종류의 미디어가 이미 등록되어 있습니다"),

    // ===== ABC (US-09) =====
    ABC_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "ABC_RECORD_NOT_FOUND", "ABC 기록을 찾을 수 없습니다"),
    ABC_RECORD_ALREADY_EXISTS(HttpStatus.CONFLICT, "ABC_RECORD_ALREADY_EXISTS", "해당 세션에 이미 ABC 기록이 존재합니다"),
    INVALID_SESSION_STATE_FOR_ABC(HttpStatus.CONFLICT, "INVALID_SESSION_STATE_FOR_ABC", "ABC 입력이 불가능한 세션 상태입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    private ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
