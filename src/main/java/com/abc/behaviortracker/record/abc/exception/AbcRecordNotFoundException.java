package com.abc.behaviortracker.record.abc.exception;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class AbcRecordNotFoundException extends BusinessException {
    public AbcRecordNotFoundException() {
        super(ErrorCode.ABC_RECORD_NOT_FOUND, ErrorCode.ABC_RECORD_NOT_FOUND.getMessage());
    }
}
