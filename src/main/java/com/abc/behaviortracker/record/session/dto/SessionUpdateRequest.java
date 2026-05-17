package com.abc.behaviortracker.record.session.dto;

import jakarta.validation.constraints.Size;

public record SessionUpdateRequest(
        @Size(max = 2000, message = "메모는 2000자를 초과할 수 없습니다")
        String memo
) {}