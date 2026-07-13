package com.abc.behaviortracker.ai.controller;

import com.abc.behaviortracker.ai.dto.AiAnalysisRequest;
import com.abc.behaviortracker.ai.dto.AiBehaviorDraftResponse;
import com.abc.behaviortracker.ai.service.AiBehaviorDraftService;
import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 행동 분석 API (ai-server 연동)")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiBehaviorDraftService aiBehaviorDraftService;

    @Operation(summary = "AI 행동 분석 요청 및 초안 저장",
            description = "ai-server 에 분석을 요청하고, 받은 결과(behavior/confidence/draftText)를 "
                    + "DRAFT 상태의 행동 기록 초안으로 저장한 뒤 반환합니다.")
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AiBehaviorDraftResponse>> analyze(
            @Valid @RequestBody AiAnalysisRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        AiBehaviorDraftResponse response =
                aiBehaviorDraftService.analyzeAndSaveDraft(principal.teacherId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
