package com.abc.behaviortracker.student.dto;

import com.abc.behaviortracker.student.domain.Student;

import java.time.LocalDate;

public record StudentResponse(
        Long studentId,
        String name,
        Integer grade,
        LocalDate birthDate
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getGrade(),
                student.getBirthDate()
        );
    }
}
