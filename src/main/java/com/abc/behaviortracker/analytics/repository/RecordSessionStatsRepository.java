package com.abc.behaviortracker.analytics.repository;

import com.abc.behaviortracker.record.session.domain.RecordSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RecordSessionStatsRepository extends JpaRepository<RecordSession, Long> {

    @Query("""
            SELECT s.status AS status, COUNT(s) AS count
            FROM RecordSession s
            WHERE s.student.id = :studentId
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            GROUP BY s.status
            """)
    List<StatusCountProjection> countSessionsByStatus(
            @Param("studentId") Long studentId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}