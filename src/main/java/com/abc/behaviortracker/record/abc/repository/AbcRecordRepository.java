package com.abc.behaviortracker.record.abc.repository;

import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AbcRecordRepository extends JpaRepository<AbcRecord, Long> {

    Optional<AbcRecord> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    /**
     * 분석용 — 특정 학생의 ABC 기록 중 행동(content_b) 텍스트만 조회한다 (US-16 최다 행동 집계).
     * 빈도 집계는 애플리케이션에서 수행한다. 세션·ABC 모두 @SQLRestriction으로 soft delete 제외.
     */
    @Query("""
            SELECT a.contentB FROM AbcRecord a
            WHERE a.session.student.id = :studentId
              AND a.contentB IS NOT NULL AND a.contentB <> ''
            """)
    List<String> findBehaviorContentsByStudentId(@Param("studentId") Long studentId);

    /**
     * 분석용 — 특정 학생의 ABC 기록 중 선행사건(content_a) 텍스트만 조회한다 (최다 선행사건 집계).
     */
    @Query("""
            SELECT a.contentA FROM AbcRecord a
            WHERE a.session.student.id = :studentId
              AND a.contentA IS NOT NULL AND a.contentA <> ''
            """)
    List<String> findAntecedentContentsByStudentId(@Param("studentId") Long studentId);
}
