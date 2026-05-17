package com.abc.behaviortracker.record.abc;

import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import com.abc.behaviortracker.record.abc.dto.AbcCreateRequest;
import com.abc.behaviortracker.record.abc.dto.AbcResponse;
import com.abc.behaviortracker.record.abc.dto.AbcUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ABC", description = "ABC 행동 기록 API (US-09)")
@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/abc")
@RequiredArgsConstructor
public class AbcController {

    private final AbcService abcService;

    @Operation(summary = "ABC 기록 생성",
            description = "ENDED 상태 세션에 ABC 기록을 추가합니다. 완전 입력 시 세션 COMPLETED, 부분 입력 시 INCOMPLETE로 전이됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AbcResponse>> create(
            @PathVariable Long sessionId,
            @Valid @RequestBody AbcCreateRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        AbcResponse response = abcService.create(sessionId, principal.teacherId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "ABC 기록 조회",
            description = "세션에 연결된 ABC 기록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AbcResponse>> get(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        AbcResponse response = abcService.get(sessionId, principal.teacherId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "ABC 기록 수정",
            description = "기존 ABC 기록의 일부 또는 전체를 수정합니다. null=변경 안 함, 빈 문자열=비우기 시맨틱. 수정 후 세션 상태가 자동 재계산됩니다.")
    @PatchMapping
    public ResponseEntity<ApiResponse<AbcResponse>> update(
            @PathVariable Long sessionId,
            @Valid @RequestBody AbcUpdateRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        AbcResponse response = abcService.update(sessionId, principal.teacherId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
