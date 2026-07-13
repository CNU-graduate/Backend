package com.abc.behaviortracker.analytics.repository;

import com.abc.behaviortracker.record.session.domain.SessionStatus;

public interface StatusCountProjection {

    SessionStatus getStatus();

    Long getCount();
}