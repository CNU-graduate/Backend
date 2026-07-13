package com.abc.behaviortracker.analytics.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 특정 학생의 전체 행동 분석 요약 응답.
 *
 * <ul>
 *     <li>{@code totalRecordCount} : 전체 행동 기록(세션) 수. 시간대별 분포 합계와 일치한다.</li>
 *     <li>{@code mostFrequentBehavior} : 가장 많이 기록된 행동(ABC의 B). 기록이 없으면 {@code null}.</li>
 *     <li>{@code mostFrequentAntecedent} : 가장 많이 기록된 선행사건(ABC의 A). 기록이 없으면 {@code null}.</li>
 *     <li>{@code peakHour} : 행동이 가장 많이 발생한 시간대. 기록이 없으면 {@code null}.</li>
 *     <li>{@code hourlyDistribution} : 0~23시 24개 시간대별 행동 발생 분포.</li>
 * </ul>
 *
 * <p>전역 Jackson 설정이 non_null이라 명시적으로 ALWAYS를 지정해, 데이터가 없어도 키가 유지되고 값만 null로 내려간다.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record AnalysisSummaryResponse(
        long totalRecordCount,
        FrequencyItem mostFrequentBehavior,
        FrequencyItem mostFrequentAntecedent,
        HourlyCount peakHour,
        List<HourlyCount> hourlyDistribution
) {
}