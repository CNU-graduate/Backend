package com.abc.behaviortracker.auth;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

/**
 * 보안상 '이메일이 없는지' '비밀번호가 틀린지' 구분해 노출하지 않습니다.
 * 둘 다 동일하게 INVALID_CREDENTIALS로 처리.
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
