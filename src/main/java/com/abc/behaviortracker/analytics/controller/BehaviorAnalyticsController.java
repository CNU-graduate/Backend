package com.abc.behaviortracker.analytics.controller;

import com.abc.behaviortracker.analytics.dto.FrequencyItemResponse;
import com.abc.behaviortracker.analytics.dto.PeriodSummaryResponse;
import com.abc.behaviortracker.analytics.service.BehaviorAnalyticsService;
import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Tag(name = "Analytics", description = "행동 데이터 통계 API (Sprint 3)")
@RestController
@RequestMapping("/api/v1/students/{studentId}/analytics")
@RequiredArgsConstructor
public class BehaviorAnalyticsController {

    private final BehaviorAnalyticsService behaviorAnalyticsService;

    @Operation(summary = "행동 빈도 분석",
            description = "ABC 기록의 행동(B) 항목을 trim 후 동일 문자열 기준으로 집계하여 빈도 내림차순(동률 시 label 오름차순)으로 반환합니다.")
    @GetMapping("/behavior-frequency")
    public ApiResponse<List<FrequencyItemResponse>> getBehaviorFrequency(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(principal.teacherId(), studentId, from, to);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "선행사건별 분석",
            description = "ABC 기록의 선행사건(A) 항목을 trim 후 동일 문자열 기준으로 집계하여 빈도 내림차순(동률 시 label 오름차순)으로 반환합니다.")
    @GetMapping("/antecedent-frequency")
    public ApiResponse<List<FrequencyItemResponse>> getAntecedentFrequency(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getAntecedentFrequency(principal.teacherId(), studentId, from, to);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "기간별 통계 조회",
            description = "지정 기간(RecordSession.startedAt 기준) 내 세션 상태별 건수, 총 세션 건수, 총 ABC 기록 건수를 반환합니다. "
                    + "from/to 미지정 시 전체 기간을 집계합니다.")
    @GetMapping("/period-summary")
    public ApiResponse<PeriodSummaryResponse> getPeriodSummary(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        PeriodSummaryResponse result =
                behaviorAnalyticsService.getPeriodSummary(principal.teacherId(), studentId, from, to);
        return ApiResponse.ok(result);
    }
}