package com.abc.behaviortracker.analytics.dto;

import com.abc.behaviortracker.record.session.domain.SessionStatus;

public record StatusCountResponse(SessionStatus status, long count) {
}