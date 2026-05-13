package com.abc.behaviortracker.student;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class StudentNotFoundException extends BusinessException {

    public StudentNotFoundException(Long studentId) {
        super(ErrorCode.STUDENT_NOT_FOUND, "Student not found: " + studentId);
    }
}
