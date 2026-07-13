package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.HourlyCount;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 행동 발생 시간대 분석 (US-16).
 *
 * <p>{@link com.abc.behaviortracker.record.session.domain.RecordSession#getStartedAt() startedAt}을 기준으로
 * 0~23시 시간대별 행동 발생 횟수를 집계하고, 최다 발생 시간대(peakHour)를 계산한다.
 * (행동 지속시간 {@code endedAt - startedAt} 분석은 본 기능 범위가 아니다.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HourlyAnalyticsService {

    /**
     * 시간대 집계 기준 타임존. startedAt은 UTC(Instant)로 저장되므로 서비스 기준 시간대(KST)로 변환해
     * 시(hour)를 산출한다. 애플리케이션 전역 타임존(application.yaml)과 동일하게 Asia/Seoul을 사용한다.
     */
    private static final ZoneId ANALYSIS_ZONE = ZoneId.of("Asia/Seoul");
    private static final int HOURS_PER_DAY = 24;

    private final RecordSessionRepository sessionRepository;
    private final AnalyticsAccessValidator accessValidator;

    /**
     * 특정 학생의 시간대별 행동 발생 분포와 최다 발생 시간대를 조회한다.
     * 행동 기록이 없어도 24개 시간대를 모두 {@code count = 0}으로 채워 반환하며, peakHour는 {@code null}이 된다.
     */
    public HourlyDistributionResponse getDistribution(Long teacherId, Long studentId) {
        accessValidator.validateOwnership(teacherId, studentId);

        List<Instant> startedAtList = sessionRepository.findStartedAtByStudentId(studentId);
        return aggregate(startedAtList);
    }

    private HourlyDistributionResponse aggregate(List<Instant> startedAtList) {
        long[] counts = new long[HOURS_PER_DAY];
        for (Instant startedAt : startedAtList) {
            int hour = startedAt.atZone(ANALYSIS_ZONE).getHour();
            counts[hour]++;
        }

        List<HourlyCount> distribution = new ArrayList<>(HOURS_PER_DAY);
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            distribution.add(new HourlyCount(hour, counts[hour]));
        }

        HourlyCount peakHour = findPeakHour(counts);
        return new HourlyDistributionResponse(distribution, peakHour);
    }

    /**
     * 최다 발생 시간대 계산. 비교에 {@code >}만 사용하므로 발생 횟수가 동일하면 가장 이른 시간대가 유지된다.
     * 모든 시간대가 0(=기록 없음)이면 {@code null}을 반환한다.
     */
    private HourlyCount findPeakHour(long[] counts) {
        int peakIndex = -1;
        long maxCount = 0;
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            if (counts[hour] > maxCount) {
                maxCount = counts[hour];
                peakIndex = hour;
            }
        }
        return peakIndex < 0 ? null : new HourlyCount(peakIndex, maxCount);
    }
}