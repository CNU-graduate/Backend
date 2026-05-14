package com.abc.behaviortracker.record.abc.exception;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.record.session.domain.SessionStatus;

public class InvalidSessionStateForAbcException extends BusinessException {
    public InvalidSessionStateForAbcException(SessionStatus currentStatus) {
        super(
                ErrorCode.INVALID_SESSION_STATE_FOR_ABC,
                "ABC 입력이 불가능한 세션 상태입니다. 현재 상태: " + currentStatus
        );
    }
}
