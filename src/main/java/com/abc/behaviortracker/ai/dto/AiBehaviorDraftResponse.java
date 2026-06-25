package com.abc.behaviortracker.ai.dto;

import com.abc.behaviortracker.ai.domain.AiBehaviorDraft;
import com.abc.behaviortracker.ai.domain.DraftStatus;

import java.time.Instant;

/**
 * 저장된 AI 행동 기록 초안 응답 DTO.
 */
public record AiBehaviorDraftResponse(
        Long draftId,
        Long trackId,
        Long studentId,
        String behavior,
        Double confidence,
        String draftText,
        DraftStatus status,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static AiBehaviorDraftResponse from(AiBehaviorDraft draft) {
        return new AiBehaviorDraftResponse(
                draft.getId(),
                draft.getTrackId(),
                draft.getStudentId(),
                draft.getBehavior(),
                draft.getConfidence(),
                draft.getDraftText(),
                draft.getStatus(),
                draft.getCreatedBy(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }
}
