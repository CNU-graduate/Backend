package com.abc.behaviortracker.record.session.dto;

import java.time.Instant;

public record SessionAbandonRequest(
        Instant lastValidAt
) {}
