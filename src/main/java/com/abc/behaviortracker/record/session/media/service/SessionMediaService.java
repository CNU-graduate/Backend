package com.abc.behaviortracker.record.session.media.service;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.media.SessionMediaAlreadyExistsException;
import com.abc.behaviortracker.record.session.media.SessionMediaNotFoundException;
import com.abc.behaviortracker.record.session.media.domain.SessionMedia;
import com.abc.behaviortracker.record.session.media.dto.SessionMediaCreateRequest;
import com.abc.behaviortracker.record.session.media.dto.SessionMediaResponse;
import com.abc.behaviortracker.record.session.media.repository.SessionMediaRepository;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionMediaService {

    private final SessionMediaRepository sessionMediaRepository;
    private final RecordSessionRepository sessionRepository;

    @Transactional
    public SessionMediaResponse create(Long teacherId, Long sessionId, SessionMediaCreateRequest request) {
        RecordSession session = findSessionOwnedBy(teacherId, sessionId);

        if (!isAttachable(session.getStatus())) {
            throw new BusinessException(
                    ErrorCode.INVALID_SESSION_STATE,
                    "미디어는 ENDED/COMPLETED/INCOMPLETE 상태의 세션에만 등록할 수 있습니다. 현재 상태: " + session.getStatus()
            );
        }

        if (request.recordingEndedAt().isBefore(request.recordingStartedAt())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "녹화 종료 시각은 시작 시각보다 빠를 수 없습니다"
            );
        }

        if (sessionMediaRepository.existsBySessionIdAndMediaType(sessionId, request.mediaType())) {
            throw new SessionMediaAlreadyExistsException(sessionId, request.mediaType());
        }

        SessionMedia media = SessionMedia.builder()
                .session(session)
                .mediaType(request.mediaType())
                .fileName(request.fileName())
                .storagePath(request.storagePath())
                .fileSizeBytes(request.fileSizeBytes())
                .mimeType(request.mimeType())
                .recordingStartedAt(request.recordingStartedAt())
                .recordingEndedAt(request.recordingEndedAt())
                .build();

        SessionMedia saved = sessionMediaRepository.save(media);

        log.info("미디어 등록 완료: mediaId={}, sessionId={}, type={}, size={}",
                saved.getId(), sessionId, saved.getMediaType(), saved.getFileSizeBytes());

        return SessionMediaResponse.from(saved);
    }

    public List<SessionMediaResponse> getList(Long teacherId, Long sessionId) {
        findSessionOwnedBy(teacherId, sessionId);

        return sessionMediaRepository.findBySessionIdOrderByMediaTypeAsc(sessionId).stream()
                .map(SessionMediaResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long teacherId, Long sessionId, Long mediaId) {
        SessionMedia media = findMediaOwnedBy(teacherId, mediaId);

        if (!media.getSession().getId().equals(sessionId)) {
            log.warn("미디어와 세션 불일치: mediaId={}, pathSessionId={}, actualSessionId={}",
                    mediaId, sessionId, media.getSession().getId());
            throw new SessionMediaNotFoundException(mediaId);
        }

        media.delete();

        log.info("미디어 삭제 완료: mediaId={}, sessionId={}, teacherId={}", mediaId, sessionId, teacherId);
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

    private SessionMedia findMediaOwnedBy(Long teacherId, Long mediaId) {
        SessionMedia media = sessionMediaRepository.findById(mediaId)
                .orElseThrow(() -> new SessionMediaNotFoundException(mediaId));

        if (!media.isOwnedBy(teacherId)) {
            log.warn("권한 없는 미디어 접근: mediaId={}, teacherId={}", mediaId, teacherId);
            throw new ForbiddenAccessException();
        }

        return media;
    }

    private boolean isAttachable(SessionStatus status) {
        return status == SessionStatus.ENDED
                || status == SessionStatus.COMPLETED
                || status == SessionStatus.INCOMPLETE;
    }
}
