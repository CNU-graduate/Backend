package com.abc.behaviortracker.record.abc.dto;

import com.abc.behaviortracker.record.abc.domain.AbcRecord;

import java.time.Instant;

public record AbcResponse(
        Long abcRecordId,
        Long sessionId,
        String contentA,
        String contentB,
        String contentC,
        boolean complete,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt
) {
    public static AbcResponse from(AbcRecord abcRecord) {
        return new AbcResponse(
                abcRecord.getId(),
                abcRecord.getSession().getId(),
                abcRecord.getContentA(),
                abcRecord.getContentB(),
                abcRecord.getContentC(),
                abcRecord.isComplete(),
                abcRecord.getCreatedBy(),
                abcRecord.getCreatedAt(),
                abcRecord.getUpdatedAt()
        );
    }
}
