package com.abc.behaviortracker.record.abc.repository;

import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbcRecordRepository extends JpaRepository<AbcRecord, Long> {

    Optional<AbcRecord> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);
}
