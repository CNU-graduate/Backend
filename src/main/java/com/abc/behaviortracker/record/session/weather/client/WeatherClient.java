package com.abc.behaviortracker.record.session.weather.client;

import com.abc.behaviortracker.record.session.weather.WeatherFetchException;
import com.abc.behaviortracker.record.session.weather.domain.WeatherCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

/**
 * OpenWeatherMap Current Weather Data API 호출용 클라이언트 (US-17).
 * 호출 실패·응답 파싱 실패는 모두 {@link WeatherFetchException}으로 정규화한다.
 */
@Slf4j
@Component
public class WeatherClient {

    private static final String CURRENT_WEATHER_PATH = "/weather";

    private final RestTemplate restTemplate;
    private final WeatherProperties properties;

    public WeatherClient(RestTemplateBuilder builder, WeatherProperties properties) {
        this.properties = properties;
        this.restTemplate = builder
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    public WeatherFetchResult fetchCurrent(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new WeatherFetchException("위/경도가 비어 있어 날씨를 가져올 수 없습니다");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new WeatherFetchException("OpenWeather API 키가 설정되지 않았습니다 (app.weather.api-key)");
        }

        URI uri = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path(CURRENT_WEATHER_PATH)
                .queryParam("lat", latitude.toPlainString())
                .queryParam("lon", longitude.toPlainString())
                .queryParam("appid", properties.getApiKey())
                .queryParam("units", properties.getUnits())
                .queryParam("lang", properties.getLang())
                .build(true)
                .toUri();

        OpenWeatherResponse response;
        try {
            response = restTemplate.getForObject(uri, OpenWeatherResponse.class);
        } catch (RestClientException e) {
            log.warn("OpenWeather 호출 실패: lat={}, lon={}, error={}", latitude, longitude, e.getMessage());
            throw new WeatherFetchException("OpenWeather 호출 실패: " + e.getMessage(), e);
        }

        return toResult(response);
    }

    private WeatherFetchResult toResult(OpenWeatherResponse response) {
        if (response == null
                || response.weather() == null
                || response.weather().isEmpty()
                || response.main() == null
                || response.main().temp() == null
                || response.main().humidity() == null) {
            throw new WeatherFetchException("OpenWeather 응답 형식이 올바르지 않습니다");
        }

        OpenWeatherResponse.Weather weather = response.weather().get(0);
        if (weather.icon() == null || weather.icon().isBlank()) {
            throw new WeatherFetchException("OpenWeather 응답에 아이콘 코드가 없습니다");
        }

        BigDecimal temperature = BigDecimal.valueOf(response.main().temp())
                .setScale(2, RoundingMode.HALF_UP);
        Instant observedAt = response.dt() != null ? Instant.ofEpochSecond(response.dt()) : Instant.now();

        return new WeatherFetchResult(
                WeatherCondition.fromOpenWeatherMain(weather.main()),
                weather.description(),
                weather.icon(),
                temperature,
                response.main().humidity(),
                response.name(),
                observedAt
        );
    }
}
