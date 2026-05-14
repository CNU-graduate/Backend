package com.abc.behaviortracker.record.session.media.dto;

import com.abc.behaviortracker.record.session.media.domain.SessionMediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record SessionMediaCreateRequest(
        @NotNull(message = "미디어 종류는 필수입니다")
        SessionMediaType mediaType,

        @NotBlank(message = "파일명은 필수입니다")
        @Size(max = 255, message = "파일명은 255자를 초과할 수 없습니다")
        String fileName,

        @NotBlank(message = "저장 경로는 필수입니다")
        @Size(max = 500, message = "저장 경로는 500자를 초과할 수 없습니다")
        String storagePath,

        @NotNull(message = "파일 크기는 필수입니다")
        @Positive(message = "파일 크기는 0보다 커야 합니다")
        Long fileSizeBytes,

        @Size(max = 100, message = "MIME 타입은 100자를 초과할 수 없습니다")
        String mimeType,

        @NotNull(message = "녹화 시작 시각은 필수입니다")
        @PastOrPresent(message = "녹화 시작 시각은 미래일 수 없습니다")
        Instant recordingStartedAt,

        @NotNull(message = "녹화 종료 시각은 필수입니다")
        @PastOrPresent(message = "녹화 종료 시각은 미래일 수 없습니다")
        Instant recordingEndedAt
) {}
