package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.FrequencyItemResponse;
import com.abc.behaviortracker.analytics.dto.PeriodSummaryResponse;
import com.abc.behaviortracker.analytics.dto.StatusCountResponse;
import com.abc.behaviortracker.analytics.repository.AbcRecordAnalyticsRepository;
import com.abc.behaviortracker.analytics.repository.RecordSessionStatsRepository;
import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.student.StudentNotFoundException;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorAnalyticsService {

    private final AbcRecordAnalyticsRepository abcRecordAnalyticsRepository;
    private final RecordSessionStatsRepository recordSessionStatsRepository;
    private final StudentRepository studentRepository;

    public List<FrequencyItemResponse> getBehaviorFrequency(
            Long teacherId, Long studentId, Instant from, Instant to
    ) {
        findStudentOwnedBy(teacherId, studentId);
        validatePeriod(from, to);

        return abcRecordAnalyticsRepository.findBehaviorFrequency(studentId, from, to).stream()
                .map(p -> new FrequencyItemResponse(p.getLabel(), p.getCount()))
                .toList();
    }

    public List<FrequencyItemResponse> getAntecedentFrequency(
            Long teacherId, Long studentId, Instant from, Instant to
    ) {
        findStudentOwnedBy(teacherId, studentId);
        validatePeriod(from, to);

        return abcRecordAnalyticsRepository.findAntecedentFrequency(studentId, from, to).stream()
                .map(p -> new FrequencyItemResponse(p.getLabel(), p.getCount()))
                .toList();
    }

    public PeriodSummaryResponse getPeriodSummary(
            Long teacherId, Long studentId, Instant from, Instant to
    ) {
        findStudentOwnedBy(teacherId, studentId);
        validatePeriod(from, to);

        List<StatusCountResponse> statusCounts = recordSessionStatsRepository
                .countSessionsByStatus(studentId, from, to).stream()
                .map(p -> new StatusCountResponse(p.getStatus(), p.getCount()))
                .toList();

        long totalSessionCount = statusCounts.stream()
                .mapToLong(StatusCountResponse::count)
                .sum();
        long totalAbcRecordCount = abcRecordAnalyticsRepository.countByStudentAndPeriod(studentId, from, to);

        return new PeriodSummaryResponse(studentId, from, to, totalSessionCount, totalAbcRecordCount, statusCounts);
    }

    private void validatePeriod(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "from은 to보다 늦을 수 없습니다");
        }
    }

    private Student findStudentOwnedBy(Long teacherId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        if (!student.isOwnedBy(teacherId)) {
            log.warn("권한 없는 학생 접근: studentId={}, teacherId={}", studentId, teacherId);
            throw new ForbiddenAccessException();
        }

        return student;
    }
}