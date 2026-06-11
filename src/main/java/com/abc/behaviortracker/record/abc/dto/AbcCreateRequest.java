package com.abc.behaviortracker.record.abc.dto;

import jakarta.validation.constraints.Size;

public record AbcCreateRequest(
        @Size(max = 1000, message = "선행사건은 1000자 이하여야 합니다")
        String contentA,

        @Size(max = 1000, message = "행동은 1000자 이하여야 합니다")
        String contentB,

        @Size(max = 1000, message = "결과는 1000자 이하여야 합니다")
        String contentC
) {}
