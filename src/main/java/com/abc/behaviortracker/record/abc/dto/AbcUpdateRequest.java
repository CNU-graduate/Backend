package com.abc.behaviortracker.record.abc.dto;

import jakarta.validation.constraints.Size;

/**
 * ABC 수정 요청 (US-11, US-24).
 *
 * <p>PATCH 시맨틱: null=변경 안 함, 빈 문자열=명시적 비우기, 값=그 값으로 변경.
 */
public record AbcUpdateRequest(
        @Size(max = 1000, message = "선행사건은 1000자 이하여야 합니다")
        String contentA,

        @Size(max = 1000, message = "행동은 1000자 이하여야 합니다")
        String contentB,

        @Size(max = 1000, message = "결과는 1000자 이하여야 합니다")
        String contentC
) {
    /**
     * 모든 필드가 null인지 검사 (아무 변경 의도가 없는 요청 차단용).
     * AbcService.update에서 이 결과가 true이면 BusinessException으로 거부.
     */
    public boolean isEmpty() {
        return contentA == null && contentB == null && contentC == null;
    }
}
