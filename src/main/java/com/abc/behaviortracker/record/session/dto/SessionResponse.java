package com.abc.behaviortracker.record.session.dto;

import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.domain.TriggerType;

import java.time.Duration;
import java.time.Instant;

public record SessionResponse(
        Long sessionId,
        Long studentId,
        String studentName,
        SessionStatus status,
        TriggerType triggerType,
        Instant startedAt,
        Instant endedAt,
        Long durationSeconds,
        boolean mediaAssisted
) {
    public static SessionResponse from(RecordSession session) {
        Duration duration = session.getDuration();
        return new SessionResponse(
                session.getId(),
                session.getStudent().getId(),
                session.getStudent().getName(),
                session.getStatus(),
                session.getTriggerType(),
                session.getStartedAt(),
                session.getEndedAt(),
                duration != null ? duration.getSeconds() : null,
                session.isMediaAssisted()
        );
    }
}
