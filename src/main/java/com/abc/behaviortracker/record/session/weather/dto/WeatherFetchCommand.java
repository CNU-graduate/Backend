package com.abc.behaviortracker.record.session.weather.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 세션 시작 시점에 날씨를 가져오기 위한 위치 정보 (US-17).
 * 위/경도는 필수, locationName은 표시·검색용 부가 정보.
 */
public record WeatherFetchCommand(

        @NotNull(message = "위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        BigDecimal longitude,

        @Size(max = 100, message = "지역명은 100자를 초과할 수 없습니다")
        String locationName
) {}
