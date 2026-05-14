package com.abc.behaviortracker.record.session.media;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.record.session.media.domain.SessionMediaType;

public class SessionMediaAlreadyExistsException extends BusinessException {

    public SessionMediaAlreadyExistsException(Long sessionId, SessionMediaType mediaType) {
        super(ErrorCode.SESSION_MEDIA_ALREADY_EXISTS,
                "SessionMedia already exists: sessionId=" + sessionId + ", mediaType=" + mediaType);
    }
}
