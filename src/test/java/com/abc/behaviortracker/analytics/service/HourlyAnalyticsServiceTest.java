package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.HourlyCount;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HourlyAnalyticsServiceTest {

    private static final Long TEACHER_ID = 1L;
    private static final Long STUDENT_ID = 10L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    RecordSessionRepository sessionRepository;
    @Mock
    AnalyticsAccessValidator accessValidator;
    @InjectMocks
    HourlyAnalyticsService hourlyAnalyticsService;

    /** 지정한 KST 시각을 저장 포맷(UTC Instant)으로 변환. */
    private static Instant kst(int hour, int minute) {
        return ZonedDateTime.of(2026, 7, 13, hour, minute, 0, 0, KST).toInstant();
    }

    private static long countAt(HourlyDistributionResponse res, int hour) {
        return res.hourlyDistribution().get(hour).count();
    }

    @Test
    @DisplayName("시간대별 분포는 항상 0~23시 24개 항목을 순서대로 반환한다")
    void returns24BucketsInOrder() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID)).thenReturn(List.of());

        HourlyDistributionResponse res = hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        assertThat(res.hourlyDistribution()).hasSize(24);
        assertThat(res.hourlyDistribution()).extracting(HourlyCount::hour)
                .containsExactlyElementsOf(IntStream.range(0, 24).boxed().toList());
    }

    @Test
    @DisplayName("KST 기준으로 시(hour)를 집계한다 - 09:10, 09:40, 13:20 → 9시 2건, 13시 1건")
    void aggregatesByKstHour() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID))
                .thenReturn(List.of(kst(9, 10), kst(9, 40), kst(13, 20)));

        HourlyDistributionResponse res = hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        assertThat(countAt(res, 9)).isEqualTo(2);
        assertThat(countAt(res, 13)).isEqualTo(1);
        assertThat(countAt(res, 0)).isZero();
        assertThat(res.peakHour()).isEqualTo(new HourlyCount(9, 2));
    }

    @Test
    @DisplayName("자정(0시)/심야(23시) 경계도 KST로 정확히 매핑된다")
    void handlesMidnightAndLateBoundary() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID))
                .thenReturn(List.of(kst(0, 30), kst(23, 30)));

        HourlyDistributionResponse res = hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        assertThat(countAt(res, 0)).isEqualTo(1);
        assertThat(countAt(res, 23)).isEqualTo(1);
    }

    @Test
    @DisplayName("최다 시간대가 동점이면 가장 이른 시간대를 반환한다")
    void peakHourTieBreaksToEarliest() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID))
                .thenReturn(List.of(kst(9, 0), kst(9, 5), kst(13, 0), kst(13, 5)));

        HourlyDistributionResponse res = hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        assertThat(res.peakHour()).isEqualTo(new HourlyCount(9, 2));
    }

    @Test
    @DisplayName("기록이 없으면 24개 0건 + peakHour는 null")
    void emptyDataReturnsNullPeak() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID)).thenReturn(List.of());

        HourlyDistributionResponse res = hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        assertThat(res.peakHour()).isNull();
        assertThat(res.hourlyDistribution()).hasSize(24).allMatch(h -> h.count() == 0);
    }

    @Test
    @DisplayName("조회 전 학생 소유권을 검증한다")
    void validatesOwnershipBeforeQuery() {
        when(sessionRepository.findStartedAtByStudentId(STUDENT_ID)).thenReturn(List.of());

        hourlyAnalyticsService.getDistribution(TEACHER_ID, STUDENT_ID);

        verify(accessValidator).validateOwnership(TEACHER_ID, STUDENT_ID);
    }
}
