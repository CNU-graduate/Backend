package com.abc.behaviortracker.record.session.media;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

public class SessionMediaNotFoundException extends BusinessException {

    public SessionMediaNotFoundException(Long mediaId) {
        super(ErrorCode.SESSION_MEDIA_NOT_FOUND, "SessionMedia not found: " + mediaId);
    }
}
