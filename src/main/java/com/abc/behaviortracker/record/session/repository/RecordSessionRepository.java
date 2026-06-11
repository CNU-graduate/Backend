package com.abc.behaviortracker.record.session.repository;

import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RecordSessionRepository extends JpaRepository<RecordSession, Long> {

    Page<RecordSession> findByStudentIdOrderByStartedAtDesc(Long studentId, Pageable pageable);

    Page<RecordSession> findByStudentIdAndStartedAtBetweenOrderByStartedAtDesc(
            Long studentId, Instant from, Instant to, Pageable pageable
    );

    Optional<RecordSession> findFirstByStudentIdAndStatusIn(Long studentId, List<SessionStatus> statuses);

    @Query("""
            SELECT s FROM RecordSession s
            WHERE s.student.id = :studentId
              AND (:status IS NULL OR s.status = :status)
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            ORDER BY s.startedAt DESC
            """)
    Page<RecordSession> findByStudentIdWithFilters(
            @Param("studentId") Long studentId,
            @Param("status") SessionStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
