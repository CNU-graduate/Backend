package com.abc.behaviortracker.analytics.dto;

/**
 * 특정 시간대(0~23시)의 행동 발생 횟수.
 * 시간대별 분포의 각 항목과 최다 발생 시간대(peakHour) 표현에 공통으로 사용한다.
 */
public record HourlyCount(int hour, long count) {
}
