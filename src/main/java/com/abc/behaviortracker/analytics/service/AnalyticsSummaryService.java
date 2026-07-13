package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.AnalysisSummaryResponse;
import com.abc.behaviortracker.analytics.dto.FrequencyItem;
import com.abc.behaviortracker.analytics.dto.HourlyCount;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전체 분석 결과 요약 조립 (US-18 결과 제공 / 요약).
 *
 * <p>시간대 분석({@link HourlyAnalyticsService})과 행동/선행사건 분석({@link BehaviorAnalyticsService}) 결과를
 * 그대로 재사용해 하나의 Summary 응답으로 조립한다. 집계 로직을 중복 작성하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsSummaryService {

    private final HourlyAnalyticsService hourlyAnalyticsService;
    private final BehaviorAnalyticsService behaviorAnalyticsService;

    public AnalysisSummaryResponse getSummary(Long teacherId, Long studentId) {
        // 시간대 분석 재사용 — 접근 권한 검증도 이 호출에서 함께 수행된다.
        HourlyDistributionResponse hourly = hourlyAnalyticsService.getDistribution(teacherId, studentId);

        // 전체 행동 기록 수 = 시간대별 분포의 합계 (세션 수와 일치)
        long totalRecordCount = hourly.hourlyDistribution().stream()
                .mapToLong(HourlyCount::count)
                .sum();

        FrequencyItem mostFrequentBehavior = behaviorAnalyticsService.getMostFrequentBehavior(teacherId, studentId);
        FrequencyItem mostFrequentAntecedent = behaviorAnalyticsService.getMostFrequentAntecedent(teacherId, studentId);

        return new AnalysisSummaryResponse(
                totalRecordCount,
                mostFrequentBehavior,
                mostFrequentAntecedent,
                hourly.peakHour(),
                hourly.hourlyDistribution()
        );
    }
}
