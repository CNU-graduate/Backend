package com.abc.behaviortracker.auth;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists: " + email);
    }
}
