package com.abc.behaviortracker.record.abc.exception;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class AbcRecordAlreadyExistsException extends BusinessException {
    public AbcRecordAlreadyExistsException() {
        super(ErrorCode.ABC_RECORD_ALREADY_EXISTS, ErrorCode.ABC_RECORD_ALREADY_EXISTS.getMessage());
    }
}
