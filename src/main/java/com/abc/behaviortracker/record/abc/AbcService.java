package com.abc.behaviortracker.record.abc;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import com.abc.behaviortracker.record.abc.domain.AbcRecordRepository;
import com.abc.behaviortracker.record.abc.dto.AbcCreateRequest;
import com.abc.behaviortracker.record.abc.dto.AbcResponse;
import com.abc.behaviortracker.record.abc.dto.AbcUpdateRequest;
import com.abc.behaviortracker.record.abc.exception.AbcRecordAlreadyExistsException;
import com.abc.behaviortracker.record.abc.exception.AbcRecordNotFoundException;
import com.abc.behaviortracker.record.abc.exception.InvalidSessionStateForAbcException;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbcService {

    private final AbcRecordRepository abcRecordRepository;
    private final RecordSessionRepository sessionRepository;

    @Transactional
    public AbcResponse create(Long sessionId, Long teacherId, AbcCreateRequest request) {
        RecordSession session = findSessionOwnedBy(sessionId, teacherId);

        if (session.getStatus() != SessionStatus.ENDED) {
            throw new InvalidSessionStateForAbcException(session.getStatus());
        }

        if (abcRecordRepository.existsBySessionId(sessionId)) {
            throw new AbcRecordAlreadyExistsException();
        }

        AbcRecord abcRecord = AbcRecord.builder()
                .session(session)
                .contentA(request.contentA())
                .contentB(request.contentB())
                .contentC(request.contentC())
                .createdBy(teacherId)
                .build();
        AbcRecord saved = abcRecordRepository.save(abcRecord);

        if (saved.isComplete()) {
            session.markCompleted();
        } else {
            session.markIncomplete();
        }

        log.info("ABC 생성: sessionId={}, teacherId={}, complete={}, sessionStatus={}",
                sessionId, teacherId, saved.isComplete(), session.getStatus());

        return AbcResponse.from(saved);
    }

    public AbcResponse get(Long sessionId, Long teacherId) {
        findSessionOwnedBy(sessionId, teacherId);

        AbcRecord abcRecord = abcRecordRepository.findBySessionId(sessionId)
                .orElseThrow(AbcRecordNotFoundException::new);

        return AbcResponse.from(abcRecord);
    }

    @Transactional
    public AbcResponse update(Long sessionId, Long teacherId, AbcUpdateRequest request) {
        // 1. 모든 필드 null이면 거부 (no-op 요청 차단)
        if (request.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "수정할 필드가 하나 이상 필요합니다"
            );
        }

        // 2. 세션 권한 체크
        RecordSession session = findSessionOwnedBy(sessionId, teacherId);

        // 3. 세션 상태 체크: ENDED, COMPLETED, INCOMPLETE만 허용
        SessionStatus status = session.getStatus();
        if (status != SessionStatus.ENDED
                && status != SessionStatus.COMPLETED
                && status != SessionStatus.INCOMPLETE) {
            throw new InvalidSessionStateForAbcException(status);
        }

        // 4. ABC 조회
        AbcRecord abcRecord = abcRecordRepository.findBySessionId(sessionId)
                .orElseThrow(AbcRecordNotFoundException::new);

        // 5. PATCH 시맨틱으로 내용 수정 (Dirty Checking)
        abcRecord.updateContent(request.contentA(), request.contentB(), request.contentC());

        // 6. 세션 상태 재계산 (멱등 보장 — 같은 상태면 markXxx가 early return)
        if (abcRecord.isComplete()) {
            session.markCompleted();
        } else {
            session.markIncomplete();
        }

        log.info("ABC 수정: sessionId={}, teacherId={}, complete={}, sessionStatus={}",
                sessionId, teacherId, abcRecord.isComplete(), session.getStatus());

        return AbcResponse.from(abcRecord);
    }

    @Transactional
    public void delete(Long sessionId, Long teacherId) {
        // 1. 세션 권한 체크
        RecordSession session = findSessionOwnedBy(sessionId, teacherId);

        // 2. 세션 상태 체크: ENDED, COMPLETED, INCOMPLETE만 허용
        SessionStatus status = session.getStatus();
        if (status != SessionStatus.ENDED
                && status != SessionStatus.COMPLETED
                && status != SessionStatus.INCOMPLETE) {
            throw new InvalidSessionStateForAbcException(status);
        }

        // 3. ABC 조회
        AbcRecord abcRecord = abcRecordRepository.findBySessionId(sessionId)
                .orElseThrow(AbcRecordNotFoundException::new);

        // 4. Soft Delete
        abcRecord.delete();

        // 5. 세션 상태를 ENDED로 복귀 (이후 POST 재허용)
        session.revertToEnded();

        log.info("ABC 삭제: sessionId={}, teacherId={}, abcRecordId={}, sessionStatus={}",
                sessionId, teacherId, abcRecord.getId(), session.getStatus());
    }

    private RecordSession findSessionOwnedBy(Long sessionId, Long teacherId) {
        RecordSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SESSION_NOT_FOUND,
                        "Session not found: " + sessionId
                ));

        if (!session.isOwnedBy(teacherId)) {
            log.warn("ABC 세션 권한 위반: teacherId={}, sessionId={}", teacherId, sessionId);
            throw new ForbiddenAccessException();
        }

        return session;
    }
}
