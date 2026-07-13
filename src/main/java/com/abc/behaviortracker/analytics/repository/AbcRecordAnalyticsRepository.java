package com.abc.behaviortracker.analytics.repository;

import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AbcRecordAnalyticsRepository extends JpaRepository<AbcRecord, Long> {

    @Query("""
            SELECT TRIM(a.contentB) AS label, COUNT(a) AS count
            FROM AbcRecord a
            JOIN a.session s
            WHERE s.student.id = :studentId
              AND a.contentB IS NOT NULL AND TRIM(a.contentB) <> ''
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            GROUP BY TRIM(a.contentB)
            ORDER BY COUNT(a) DESC, TRIM(a.contentB) ASC
            """)
    List<FrequencyProjection> findBehaviorFrequency(
            @Param("studentId") Long studentId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            SELECT TRIM(a.contentA) AS label, COUNT(a) AS count
            FROM AbcRecord a
            JOIN a.session s
            WHERE s.student.id = :studentId
              AND a.contentA IS NOT NULL AND TRIM(a.contentA) <> ''
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            GROUP BY TRIM(a.contentA)
            ORDER BY COUNT(a) DESC, TRIM(a.contentA) ASC
            """)
    List<FrequencyProjection> findAntecedentFrequency(
            @Param("studentId") Long studentId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            SELECT COUNT(a)
            FROM AbcRecord a
            JOIN a.session s
            WHERE s.student.id = :studentId
              AND (:from IS NULL OR s.startedAt >= :from)
              AND (:to IS NULL OR s.startedAt <= :to)
            """)
    long countByStudentAndPeriod(
            @Param("studentId") Long studentId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}