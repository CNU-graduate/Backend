package com.abc.behaviortracker.record.session.repository;

import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RecordSessionRepository extends JpaRepository<RecordSession, Long> {

    Page<RecordSession> findByStudentIdOrderByStartedAtDesc(Long studentId, Pageable pageable);

    Page<RecordSession> findByStudentIdAndStartedAtBetweenOrderByStartedAtDesc(
            Long studentId, Instant from, Instant to, Pageable pageable
    );

    Optional<RecordSession> findFirstByStudentIdAndStatusIn(Long studentId, List<SessionStatus> statuses);
}
