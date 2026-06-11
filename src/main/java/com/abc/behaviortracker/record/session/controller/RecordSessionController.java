package com.abc.behaviortracker.record.session.controller;

import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.common.PageResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.dto.SessionAbandonRequest;
import com.abc.behaviortracker.record.session.dto.SessionResponse;
import com.abc.behaviortracker.record.session.dto.SessionStartRequest;
import com.abc.behaviortracker.record.session.dto.SessionUpdateRequest;
import com.abc.behaviortracker.record.session.service.RecordSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "RecordSession", description = "행동 기록 세션 API")
public class RecordSessionController {

    private final RecordSessionService sessionService;

    @Operation(summary = "행동 기록 시작",
            description = "지정 학생에 대해 새 기록 세션을 시작합니다. 시작 시각은 서버가 발급. 활성 세션이 이미 있으면 409.")
    @PostMapping("/api/v1/students/{studentId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionResponse> start(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestBody SessionStartRequest request
    ) {
        SessionResponse result = sessionService.start(principal.teacherId(), studentId, request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "행동 기록 종료",
            description = "RECORDING 상태의 세션을 ENDED로 전이합니다. 잘못된 상태에서 호출 시 409.")
    @PostMapping("/api/v1/sessions/{sessionId}/end")
    public ApiResponse<SessionResponse> end(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId
    ) {
        SessionResponse result = sessionService.end(principal.teacherId(), sessionId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "행동 기록 강제 종료",
            description = "앱 비정상 종료 등으로 세션을 ABANDONED 상태로 전이. lastValidAt 미지정 시 서버 시각 사용. 이미 종료된 세션은 멱등 동작.")
    @PostMapping("/api/v1/sessions/{sessionId}/abandon")
    public ApiResponse<SessionResponse> abandon(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId,
            @RequestBody(required = false) SessionAbandonRequest request
    ) {
        SessionResponse result = sessionService.abandon(principal.teacherId(), sessionId, request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "학생의 행동 기록 세션 목록",
            description = "지정 학생의 기록 세션을 최신순으로 페이징 조회. status, from, to 쿼리 파라미터로 필터링 가능 (모두 선택).")
    @GetMapping("/api/v1/students/{studentId}/sessions")
    public ApiResponse<PageResponse<SessionResponse>> getList(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<SessionResponse> page = sessionService.getList(
                principal.teacherId(), studentId, status, from, to, pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @Operation(summary = "세션 상세 조회")
    @GetMapping("/api/v1/sessions/{sessionId}")
    public ApiResponse<SessionResponse> getDetail(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId
    ) {
        SessionResponse result = sessionService.getDetail(principal.teacherId(), sessionId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "세션 메모 수정",
            description = "PATCH 시맨틱: memo가 null이면 변경하지 않음. 빈 문자열은 메모 제거로 해석.")
    @PatchMapping("/api/v1/sessions/{sessionId}")
    public ApiResponse<SessionResponse> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionUpdateRequest request
    ) {
        SessionResponse result = sessionService.updateMemo(principal.teacherId(), sessionId, request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "세션 삭제", description = "Soft Delete. deleted_at만 설정되며 실제 데이터는 보존.")
    @DeleteMapping("/api/v1/sessions/{sessionId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId
    ) {
        sessionService.delete(principal.teacherId(), sessionId);
        return ApiResponse.ok();
    }
}
