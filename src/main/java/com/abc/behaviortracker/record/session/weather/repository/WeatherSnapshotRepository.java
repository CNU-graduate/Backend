package com.abc.behaviortracker.record.session.weather.repository;

import com.abc.behaviortracker.record.session.weather.domain.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {

    Optional<WeatherSnapshot> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);
}
