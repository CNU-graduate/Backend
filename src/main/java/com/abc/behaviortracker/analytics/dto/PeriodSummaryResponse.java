package com.abc.behaviortracker.analytics.dto;

import java.time.Instant;
import java.util.List;

public record PeriodSummaryResponse(
        Long studentId,
        Instant from,
        Instant to,
        long totalSessionCount,
        long totalAbcRecordCount,
        List<StatusCountResponse> statusCounts
) {
}