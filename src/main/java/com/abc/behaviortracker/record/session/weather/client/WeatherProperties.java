package com.abc.behaviortracker.record.session.weather.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yaml의 {@code app.weather} 블록과 바인딩되는 OpenWeatherMap 설정.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.weather")
public class WeatherProperties {

    /** OpenWeatherMap API 키. */
    private String apiKey;

    /** API 베이스 URL. 기본값: {@code https://api.openweathermap.org/data/2.5}. */
    private String baseUrl = "https://api.openweathermap.org/data/2.5";

    /** 단위계 (metric: 섭씨, imperial: 화씨). */
    private String units = "metric";

    /** 응답 언어 코드 (예: kr, en). */
    private String lang = "kr";

    /** 외부 호출 연결 타임아웃(ms). */
    private int connectTimeoutMs = 2000;

    /** 외부 호출 읽기 타임아웃(ms). */
    private int readTimeoutMs = 3000;
}
