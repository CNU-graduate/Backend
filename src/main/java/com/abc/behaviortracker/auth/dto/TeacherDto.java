package com.abc.behaviortracker.auth.dto;

import com.abc.behaviortracker.teacher.domain.Teacher;

import java.time.Instant;

public record TeacherDto(
        Long teacherId,
        String email,
        String name,
        String schoolName,
        String role,
        Instant createdAt
) {
    public static TeacherDto from(Teacher teacher) {
        return new TeacherDto(
                teacher.getId(),
                teacher.getEmail(),
                teacher.getName(),
                teacher.getSchoolName(),
                teacher.getRole().name(),
                teacher.getCreatedAt()
        );
    }
}
