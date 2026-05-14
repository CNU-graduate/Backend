package com.abc.behaviortracker.record.session.media.repository;

import com.abc.behaviortracker.record.session.media.domain.SessionMedia;
import com.abc.behaviortracker.record.session.media.domain.SessionMediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionMediaRepository extends JpaRepository<SessionMedia, Long> {

    List<SessionMedia> findBySessionIdOrderByMediaTypeAsc(Long sessionId);

    boolean existsBySessionIdAndMediaType(Long sessionId, SessionMediaType mediaType);
}
