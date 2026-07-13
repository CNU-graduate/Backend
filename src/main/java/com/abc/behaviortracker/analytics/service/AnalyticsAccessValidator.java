package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.student.StudentNotFoundException;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 분석 API 공통 접근 제어 — 대상 학생이 존재하며 요청 교사의 소유인지 검증한다.
 * 각 분석 Service가 동일 로직을 중복 구현하지 않도록 분리했다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class AnalyticsAccessValidator {

    private final StudentRepository studentRepository;

    void validateOwnership(Long teacherId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        if (!student.isOwnedBy(teacherId)) {
            log.warn("권한 없는 학생 분석 접근: studentId={}, teacherId={}", studentId, teacherId);
            throw new ForbiddenAccessException();
        }
    }
}
