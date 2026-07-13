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

    /**
     * 시간대 분석용 — 특정 학생의 모든 기록 세션 시작 시각만 조회한다 (US-16).
     * 시(hour) 집계는 타임존 변환 이식성(H2/PostgreSQL)을 위해 애플리케이션에서 수행하므로
     * 엔티티 전체가 아닌 startedAt만 가져온다. Soft delete된 세션은 @SQLRestriction으로 제외된다.
     */
    @Query("SELECT s.startedAt FROM RecordSession s WHERE s.student.id = :studentId")
    List<Instant> findStartedAtByStudentId(@Param("studentId") Long studentId);
}
