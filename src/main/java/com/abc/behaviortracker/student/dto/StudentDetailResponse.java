package com.abc.behaviortracker.student.dto;

import com.abc.behaviortracker.student.domain.Student;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record StudentDetailResponse(
        Long studentId,
        String name,
        Integer grade,
        LocalDate birthDate,
        String iepSummary,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
    public static StudentDetailResponse from(Student student) {
        return new StudentDetailResponse(
                student.getId(),
                student.getName(),
                student.getGrade(),
                student.getBirthDate(),
                student.getIepSummary(),
                student.getMetadata(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
