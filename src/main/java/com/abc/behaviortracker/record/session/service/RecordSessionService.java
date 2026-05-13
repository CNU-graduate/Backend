package com.abc.behaviortracker.record.session.service;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.dto.SessionAbandonRequest;
import com.abc.behaviortracker.record.session.dto.SessionResponse;
import com.abc.behaviortracker.record.session.dto.SessionStartRequest;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import com.abc.behaviortracker.student.StudentNotFoundException;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordSessionService {

    private final RecordSessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public SessionResponse start(Long teacherId, Long studentId, SessionStartRequest request) {
        Student student = findStudentOwnedBy(teacherId, studentId);

        sessionRepository.findFirstByStudentIdAndStatusIn(
                studentId,
                List.of(SessionStatus.INIT, SessionStatus.RECORDING)
        ).ifPresent(existing -> {
            throw new BusinessException(
                    ErrorCode.ACTIVE_SESSION_EXISTS,
                    "이미 진행 중인 세션이 있습니다: sessionId=" + existing.getId()
            );
        });

        RecordSession session = RecordSession.builder()
                .teacher(student.getTeacher())
                .student(student)
                .triggerType(request.triggerType())
                .mediaAssisted(request.mediaAssisted())
                .build();

        RecordSession saved = sessionRepository.save(session);

        log.info("세션 시작: sessionId={}, studentId={}, teacherId={}, trigger={}",
                saved.getId(), studentId, teacherId, request.triggerType());

        return SessionResponse.from(saved);
    }

    @Transactional
    public SessionResponse end(Long teacherId, Long sessionId) {
        RecordSession session = findSessionOwnedBy(teacherId, sessionId);

        session.end();

        log.info("세션 종료: sessionId={}, teacherId={}", sessionId, teacherId);

        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse abandon(Long teacherId, Long sessionId, SessionAbandonRequest request) {
        RecordSession session = findSessionOwnedBy(teacherId, sessionId);

        Instant lastValidAt = request != null ? request.lastValidAt() : null;
        session.abandon(lastValidAt);

        log.info("세션 강제 종료: sessionId={}, teacherId={}, lastValidAt={}",
                sessionId, teacherId, lastValidAt);

        return SessionResponse.from(session);
    }

    public Page<SessionResponse> getList(Long teacherId, Long studentId, Pageable pageable) {
        findStudentOwnedBy(teacherId, studentId);

        return sessionRepository.findByStudentIdOrderByStartedAtDesc(studentId, pageable)
                .map(SessionResponse::from);
    }

    public SessionResponse getDetail(Long teacherId, Long sessionId) {
        RecordSession session = findSessionOwnedBy(teacherId, sessionId);
        return SessionResponse.from(session);
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

    private RecordSession findSessionOwnedBy(Long teacherId, Long sessionId) {
        RecordSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND,
                        "Session not found: " + sessionId));

        if (!session.isOwnedBy(teacherId)) {
            log.warn("권한 없는 세션 접근: sessionId={}, teacherId={}", sessionId, teacherId);
            throw new ForbiddenAccessException();
        }

        return session;
    }
}
