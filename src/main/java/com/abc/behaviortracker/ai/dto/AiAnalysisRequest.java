package com.abc.behaviortracker.ai.dto;

import java.time.Instant;

/**
 * 외부 AI 분석 서버(ai-server)의 /analyze 호출 요청 DTO.
 *
 * <p>※ ai-server 스펙이 아직 확정되지 않았다. 현재는 trackId/studentId/timestamp 수준으로
 * 단순하게 시작하며, 실제 영상 분석 입력(프레임 구간, 영상 식별자 등)이 정해지면
 * <b>ai-server 스펙 확정되면 필드 추가/수정 필요</b>.
 *
 * @param trackId   분석 대상 트랙 식별자 (기존 ID 컨벤션에 맞춰 Long 사용)
 * @param studentId 학생 식별자. 좌석 매핑 전일 수 있으므로 <b>nullable</b>
 * @param timestamp 분석 기준 시각
 */
public record AiAnalysisRequest(
        Long trackId,
        Long studentId,
        Instant timestamp
) {
}
