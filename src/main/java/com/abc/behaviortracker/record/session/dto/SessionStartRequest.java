package com.abc.behaviortracker.record.session.dto;

import com.abc.behaviortracker.record.session.domain.TriggerType;
import jakarta.validation.constraints.NotNull;

public record SessionStartRequest(
        @NotNull(message = "트리거 타입은 필수입니다")
        TriggerType triggerType,

        boolean mediaAssisted
) {}
