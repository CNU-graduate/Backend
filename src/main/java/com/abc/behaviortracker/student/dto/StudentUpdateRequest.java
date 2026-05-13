package com.abc.behaviortracker.student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record StudentUpdateRequest(
        @Min(value = 1, message = "학년은 1 이상이어야 합니다")
        @Max(value = 12, message = "학년은 12 이하여야 합니다")
        Integer grade,

        @Size(max = 1000, message = "IEP 요약은 1000자를 초과할 수 없습니다")
        String iepSummary,

        Map<String, Object> metadata
) {}
