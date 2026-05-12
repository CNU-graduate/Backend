package com.abc.behaviortracker.global.exception;

public class ForbiddenAccessException extends BusinessException {

    public ForbiddenAccessException() {
        super(ErrorCode.FORBIDDEN_ACCESS);
    }

    public ForbiddenAccessException(String detail) {
        super(ErrorCode.FORBIDDEN_ACCESS, detail);
    }
}
