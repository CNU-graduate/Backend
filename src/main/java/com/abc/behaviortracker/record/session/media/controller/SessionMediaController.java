package com.abc.behaviortracker.record.session.media.controller;

import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import com.abc.behaviortracker.record.session.media.dto.SessionMediaCreateRequest;
import com.abc.behaviortracker.record.session.media.dto.SessionMediaResponse;
import com.abc.behaviortracker.record.session.media.service.SessionMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/media")
@RequiredArgsConstructor
@Tag(name = "SessionMedia", description = "행동 기록 세션의 영상/음성 메타데이터 API")
public class SessionMediaController {

    private final SessionMediaService sessionMediaService;

    @Operation(summary = "미디어 메타데이터 등록",
            description = "ENDED/COMPLETED/INCOMPLETE 상태의 세션에 영상 또는 음성 메타데이터를 등록합니다. "
                    + "세션당 각 종류별 최대 1개. 같은 종류가 이미 있으면 409.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionMediaResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionMediaCreateRequest request
    ) {
        SessionMediaResponse result = sessionMediaService.create(principal.teacherId(), sessionId, request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "세션의 미디어 목록 조회",
            description = "해당 세션에 등록된 미디어 메타데이터(0~2개)를 종류 오름차순으로 반환합니다.")
    @GetMapping
    public ApiResponse<List<SessionMediaResponse>> getList(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId
    ) {
        List<SessionMediaResponse> result = sessionMediaService.getList(principal.teacherId(), sessionId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "미디어 메타데이터 삭제", description = "Soft Delete. deleted_at만 설정되며 실제 데이터는 보존.")
    @DeleteMapping("/{mediaId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long sessionId,
            @PathVariable Long mediaId
    ) {
        sessionMediaService.delete(principal.teacherId(), sessionId, mediaId);
        return ApiResponse.ok();
    }
}
