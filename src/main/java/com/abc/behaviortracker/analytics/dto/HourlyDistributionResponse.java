package com.abc.behaviortracker.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 행동 발생 시간대별 분석 응답.
 *
 * <p>{@code hourlyDistribution}은 0시부터 23시까지 항상 24개 항목을 포함하며,
 * 행동 기록이 없는 시간대도 {@code count = 0}으로 채워 프론트엔드가 그래프를 바로 그릴 수 있도록 한다.
 *
 * <p>{@code peakHour}는 행동이 가장 많이 발생한 시간대이며, 기록이 전혀 없으면 {@code null}이다.
 * (전역 Jackson 설정이 non_null이라 명시적으로 ALWAYS를 지정해 null도 키가 유지되도록 한다.)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record HourlyDistributionResponse(
        List<HourlyCount> hourlyDistribution,
        HourlyCount peakHour
) {
}