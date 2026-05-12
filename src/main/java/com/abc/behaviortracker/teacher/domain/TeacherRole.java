package com.abc.behaviortracker.teacher.domain;

public enum TeacherRole {
    /** 일반 특수교사. 본인이 등록한 학생/기록만 관리할 수 있다. */
    TEACHER,

    /** 시스템 관리자. 전체 데이터에 접근할 수 있다. */
    ADMIN
}
