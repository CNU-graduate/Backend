package com.abc.behaviortracker.record.session.weather.domain;

/**
 * OpenWeatherMap의 weather[0].main 그룹을 그대로 매핑한 enum (US-17).
 * 알 수 없는 값이 내려오는 경우에 대비해 {@link #UNKNOWN}을 보유한다.
 */
public enum WeatherCondition {

    CLEAR,
    CLOUDS,
    RAIN,
    DRIZZLE,
    THUNDERSTORM,
    SNOW,
    ATMOSPHERE,
    UNKNOWN;

    public static WeatherCondition fromOpenWeatherMain(String main) {
        if (main == null || main.isBlank()) {
            return UNKNOWN;
        }
        return switch (main.trim().toUpperCase()) {
            case "CLEAR" -> CLEAR;
            case "CLOUDS" -> CLOUDS;
            case "RAIN" -> RAIN;
            case "DRIZZLE" -> DRIZZLE;
            case "THUNDERSTORM" -> THUNDERSTORM;
            case "SNOW" -> SNOW;
            case "MIST", "SMOKE", "HAZE", "DUST", "FOG", "SAND", "ASH", "SQUALL", "TORNADO" -> ATMOSPHERE;
            default -> UNKNOWN;
        };
    }
}
