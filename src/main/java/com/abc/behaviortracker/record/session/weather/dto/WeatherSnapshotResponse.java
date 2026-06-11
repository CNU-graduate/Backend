package com.abc.behaviortracker.record.session.weather.dto;

import com.abc.behaviortracker.record.session.weather.domain.WeatherCondition;
import com.abc.behaviortracker.record.session.weather.domain.WeatherSnapshot;

import java.math.BigDecimal;
import java.time.Instant;

public record WeatherSnapshotResponse(
        Long weatherSnapshotId,
        Long sessionId,
        WeatherCondition condition,
        String conditionDescription,
        String iconCode,
        BigDecimal temperatureCelsius,
        Integer humidityPercent,
        BigDecimal latitude,
        BigDecimal longitude,
        String locationName,
        Instant observedAt,
        Instant createdAt
) {
    public static WeatherSnapshotResponse from(WeatherSnapshot snapshot) {
        return new WeatherSnapshotResponse(
                snapshot.getId(),
                snapshot.getSession().getId(),
                snapshot.getCondition(),
                snapshot.getConditionDescription(),
                snapshot.getIconCode(),
                snapshot.getTemperatureCelsius(),
                snapshot.getHumidityPercent(),
                snapshot.getLatitude(),
                snapshot.getLongitude(),
                snapshot.getLocationName(),
                snapshot.getObservedAt(),
                snapshot.getCreatedAt()
        );
    }
}
