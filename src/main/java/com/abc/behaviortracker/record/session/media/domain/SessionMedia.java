package com.abc.behaviortracker.record.session.media.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.Instant;

@Getter
@Entity
@Table(name = "session_media",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_session_media_session_type",
                columnNames = {"session_id", "media_type"}
        ),
        indexes = @Index(name = "idx_session_media_session", columnList = "session_id"))
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionMedia extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RecordSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private SessionMediaType mediaType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "recording_started_at", nullable = false)
    private Instant recordingStartedAt;

    @Column(name = "recording_ended_at", nullable = false)
    private Instant recordingEndedAt;

    @Builder
    private SessionMedia(RecordSession session,
                         SessionMediaType mediaType,
                         String fileName,
                         String storagePath,
                         Long fileSizeBytes,
                         String mimeType,
                         Instant recordingStartedAt,
                         Instant recordingEndedAt) {
        this.session = session;
        this.mediaType = mediaType;
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.recordingStartedAt = recordingStartedAt;
        this.recordingEndedAt = recordingEndedAt;
    }

    public void delete() {
        markDeleted();
    }

    public Duration getDuration() {
        return Duration.between(recordingStartedAt, recordingEndedAt);
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.session != null && this.session.isOwnedBy(teacherId);
    }
}
