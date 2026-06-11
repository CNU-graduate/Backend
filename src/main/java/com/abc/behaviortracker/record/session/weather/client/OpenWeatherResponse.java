package com.abc.behaviortracker.record.session.weather.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * OpenWeatherMap Current Weather Data API 응답 매핑용 내부 DTO.
 * 클라이언트 패키지 바깥으로 노출하지 말 것 — 서비스 계층은 {@link WeatherFetchResult}만 사용한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
        List<Weather> weather,
        Main main,
        Long dt,
        String name
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(Integer id, String main, String description, String icon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(Double temp, Integer humidity) {}
}
