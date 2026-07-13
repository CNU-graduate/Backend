package com.abc.behaviortracker.analytics.dto;

/**
 * 빈도 집계 항목(이름 + 발생 횟수).
 * 최다 행동(mostFrequentBehavior)과 최다 선행사건(mostFrequentAntecedent) 표현에 공통으로 사용한다.
 */
public record FrequencyItem(String name, long count) {
}
