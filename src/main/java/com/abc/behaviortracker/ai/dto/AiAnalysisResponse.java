package com.abc.behaviortracker.ai.dto;

/**
 * 외부 AI 분석 서버(ai-server)의 /analyze 호출 응답 DTO.
 *
 * <p>behavior / confidence / draftText 3개 필드는 <b>고정</b>이다.
 * ai-server 스펙이 확정되면 식별용 필드(예: trackId echo, analyzedAt 등)를 추가할 수 있다.
 *
 * @param behavior   행동 코드 (예: "LEAVING_SEAT", "HEAD_HITTING")
 * @param confidence 신뢰도 (0.0 ~ 1.0)
 * @param draftText  교사에게 보여줄 초안 설명 텍스트
 */
public record AiAnalysisResponse(
        String behavior,
        Double confidence,
        String draftText
) {
}
