package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.AnalysisSummaryResponse;
import com.abc.behaviortracker.analytics.dto.FrequencyItem;
import com.abc.behaviortracker.analytics.dto.FrequencyItemResponse;
import com.abc.behaviortracker.analytics.dto.HourlyCount;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 전체 분석 결과 요약 조립 (US-18 결과 제공 / 요약).
 *
 * <p>시간대 분석({@link HourlyAnalyticsService})과 행동/선행사건 빈도 분석({@link BehaviorAnalyticsService}) 결과를
 * 그대로 재사용해 하나의 Summary 응답으로 조립한다. 집계 로직을 중복 작성하지 않는다.
 * 최다 행동/선행사건은 빈도 분석 결과(횟수 내림차순, 동률 시 label 오름차순)의 첫 번째 항목을 사용한다.
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

        // 빈도 분석(전체 기간) 결과의 최상위 항목을 최다 행동/선행사건으로 사용한다.
        FrequencyItem mostFrequentBehavior =
                topItem(behaviorAnalyticsService.getBehaviorFrequency(teacherId, studentId, null, null));
        FrequencyItem mostFrequentAntecedent =
                topItem(behaviorAnalyticsService.getAntecedentFrequency(teacherId, studentId, null, null));

        return new AnalysisSummaryResponse(
                totalRecordCount,
                mostFrequentBehavior,
                mostFrequentAntecedent,
                hourly.peakHour(),
                hourly.hourlyDistribution()
        );
    }

    /** 빈도 리스트(내림차순 정렬)의 최상위 항목을 요약용 {name, count}로 변환. 비어 있으면 null. */
    private FrequencyItem topItem(List<FrequencyItemResponse> frequencies) {
        if (frequencies.isEmpty()) {
            return null;
        }
        FrequencyItemResponse top = frequencies.get(0);
        return new FrequencyItem(top.label(), top.count());
    }
}
