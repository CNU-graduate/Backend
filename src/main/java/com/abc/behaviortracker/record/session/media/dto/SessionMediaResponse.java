package com.abc.behaviortracker.record.session.media.dto;

import com.abc.behaviortracker.record.session.media.domain.SessionMedia;
import com.abc.behaviortracker.record.session.media.domain.SessionMediaType;

import java.time.Duration;
import java.time.Instant;

public record SessionMediaResponse(
        Long mediaId,
        Long sessionId,
        SessionMediaType mediaType,
        String fileName,
        String storagePath,
        Long fileSizeBytes,
        String mimeType,
        Instant recordingStartedAt,
        Instant recordingEndedAt,
        Long durationSeconds,
        Instant createdAt
) {
    public static SessionMediaResponse from(SessionMedia media) {
        Duration duration = media.getDuration();
        return new SessionMediaResponse(
                media.getId(),
                media.getSession().getId(),
                media.getMediaType(),
                media.getFileName(),
                media.getStoragePath(),
                media.getFileSizeBytes(),
                media.getMimeType(),
                media.getRecordingStartedAt(),
                media.getRecordingEndedAt(),
                duration != null ? duration.getSeconds() : null,
                media.getCreatedAt()
        );
    }
}
