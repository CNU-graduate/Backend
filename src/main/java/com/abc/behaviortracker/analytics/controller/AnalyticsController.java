package com.abc.behaviortracker.analytics.controller;

import com.abc.behaviortracker.analytics.dto.AnalysisSummaryResponse;
import com.abc.behaviortracker.analytics.dto.HourlyDistributionResponse;
import com.abc.behaviortracker.analytics.service.AnalyticsSummaryService;
import com.abc.behaviortracker.analytics.service.HourlyAnalyticsService;
import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 행동 분석 API (US-16 시간대 분석 / 분석 요약).
 *
 * <p>여러 담당자가 분석 기능을 나눠 개발하므로, 실제 집계 로직은 기능별 Service에 분리되어 있고
 * 본 컨트롤러는 API 매핑만 담당한다. 신규 분석 엔드포인트는 이 컨트롤러에 통합한다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "행동 분석 API")
public class AnalyticsController {

    private final HourlyAnalyticsService hourlyAnalyticsService;
    private final AnalyticsSummaryService analyticsSummaryService;

    @Operation(summary = "행동 발생 시간대별 분포 조회",
            description = "학생의 RecordSession.startedAt(Asia/Seoul 기준)으로 0~23시 24개 시간대별 행동 발생 횟수와 "
                    + "최다 발생 시간대(peakHour)를 반환. 기록이 없는 시간대도 count 0으로 포함하며, 기록이 전혀 없으면 peakHour는 null.")
    @GetMapping("/api/v1/students/{studentId}/analytics/hourly-distribution")
    public ApiResponse<HourlyDistributionResponse> hourlyDistribution(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId
    ) {
        HourlyDistributionResponse result =
                hourlyAnalyticsService.getDistribution(principal.teacherId(), studentId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "학생 행동 분석 요약 조회",
            description = "전체 행동 기록 수, 최다 행동, 최다 선행사건, 최다 발생 시간대, 0~23시 시간대별 분포를 한 번에 반환. "
                    + "조회 전용이며 원본 기록을 변경하지 않는다. 기록이 없는 항목은 null로 내려간다.")
    @GetMapping("/api/v1/students/{studentId}/analytics/summary")
    public ApiResponse<AnalysisSummaryResponse> summary(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId
    ) {
        AnalysisSummaryResponse result =
                analyticsSummaryService.getSummary(principal.teacherId(), studentId);
        return ApiResponse.ok(result);
    }
}
