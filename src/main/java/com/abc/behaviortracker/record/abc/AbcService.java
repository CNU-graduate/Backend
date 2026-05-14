package com.abc.behaviortracker.record.abc;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import com.abc.behaviortracker.record.abc.domain.AbcRecordRepository;
import com.abc.behaviortracker.record.abc.dto.AbcCreateRequest;
import com.abc.behaviortracker.record.abc.dto.AbcResponse;
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
