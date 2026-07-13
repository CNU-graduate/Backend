package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.AnalysisSummaryResponse;
import com.abc.behaviortracker.analytics.dto.FrequencyItem;
import com.abc.behaviortracker.analytics.dto.FrequencyItemResponse;
import com.abc.behaviortracker.analytics.dto.HourlyCount;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsSummaryServiceTest {

    private static final Long TEACHER_ID = 1L;
    private static final Long STUDENT_ID = 10L;

    @Mock
    HourlyAnalyticsService hourlyAnalyticsService;
    @Mock
    BehaviorAnalyticsService behaviorAnalyticsService;
    @InjectMocks
    AnalyticsSummaryService analyticsSummaryService;

    /** hourCount 맵 없이 특정 시간대에만 값을 넣은 24칸 분포 생성. */
    private static List<HourlyCount> distributionWith(int hourA, long countA, int hourB, long countB) {
        List<HourlyCount> dist = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            long c = (h == hourA) ? countA : (h == hourB) ? countB : 0L;
            dist.add(new HourlyCount(h, c));
        }
        return dist;
    }

    private static List<HourlyCount> emptyDistribution() {
        List<HourlyCount> dist = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            dist.add(new HourlyCount(h, 0L));
        }
        return dist;
    }

    @Test
    @DisplayName("시간대/행동 빈도 분석 결과를 조립하고, 최다 항목은 빈도 리스트의 첫 항목을 사용한다")
    void assemblesSummaryReusingSubServices() {
        List<HourlyCount> dist = distributionWith(9, 2, 13, 1);
        when(hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID))
                .thenReturn(new HourlyDistributionResponse(dist, new HourlyCount(9, 2)));
        // 빈도 리스트는 내림차순 정렬 — 첫 항목이 최다. 요약은 첫 항목만 사용한다.
        when(behaviorAnalyticsService.getBehaviorFrequency(TEACHER_ID, STUDENT_ID, null, null))
                .thenReturn(List.of(new FrequencyItemResponse("자리 이탈", 8), new FrequencyItemResponse("떠들기", 3)));
        when(behaviorAnalyticsService.getAntecedentFrequency(TEACHER_ID, STUDENT_ID, null, null))
                .thenReturn(List.of(new FrequencyItemResponse("과제 수행", 10)));

        AnalysisSummaryResponse res = analyticsSummaryService.getSummary(TEACHER_ID, STUDENT_ID);

        assertThat(res.totalRecordCount()).isEqualTo(3);
        assertThat(res.mostFrequentBehavior()).isEqualTo(new FrequencyItem("자리 이탈", 8));
        assertThat(res.mostFrequentAntecedent()).isEqualTo(new FrequencyItem("과제 수행", 10));
        assertThat(res.peakHour()).isEqualTo(new HourlyCount(9, 2));
        assertThat(res.hourlyDistribution()).isSameAs(dist);
    }

    @Test
    @DisplayName("기록이 없는 학생은 total 0 + 각 항목 null, 분포는 24개 유지")
    void emptyStudentReturnsZeroAndNulls() {
        List<HourlyCount> dist = emptyDistribution();
        when(hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID))
                .thenReturn(new HourlyDistributionResponse(dist, null));
        when(behaviorAnalyticsService.getBehaviorFrequency(TEACHER_ID, STUDENT_ID, null, null))
                .thenReturn(List.of());
        when(behaviorAnalyticsService.getAntecedentFrequency(TEACHER_ID, STUDENT_ID, null, null))
                .thenReturn(List.of());

        AnalysisSummaryResponse res = analyticsSummaryService.getSummary(TEACHER_ID, STUDENT_ID);

        assertThat(res.totalRecordCount()).isZero();
        assertThat(res.mostFrequentBehavior()).isNull();
        assertThat(res.mostFrequentAntecedent()).isNull();
        assertThat(res.peakHour()).isNull();
        assertThat(res.hourlyDistribution()).hasSize(24);
    }
}
