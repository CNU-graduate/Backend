package com.abc.behaviortracker.record.session.weather.client;

import com.abc.behaviortracker.record.session.weather.domain.WeatherCondition;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 외부 날씨 API의 응답을 도메인 친화적인 형태로 정규화한 결과.
 * Provider(OpenWeather 등)가 바뀌어도 서비스 계층은 이 타입에만 의존한다.
 */
public record WeatherFetchResult(
        WeatherCondition condition,
        String conditionDescription,
        String iconCode,
        BigDecimal temperatureCelsius,
        Integer humidityPercent,
        String locationName,
        Instant observedAt
) {}
