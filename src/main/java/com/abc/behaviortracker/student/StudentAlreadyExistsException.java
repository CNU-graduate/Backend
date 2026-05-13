package com.abc.behaviortracker.student;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class StudentAlreadyExistsException extends BusinessException {

    public StudentAlreadyExistsException(String name) {
        super(ErrorCode.STUDENT_ALREADY_EXISTS, "Student already exists: " + name);
    }
}
