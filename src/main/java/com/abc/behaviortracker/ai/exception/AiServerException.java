package com.abc.behaviortracker.ai.exception;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

/**
 * 외부 AI 분석 서버(ai-server) 호출 실패 시 던지는 예외.
 *
 * <p>연결 실패 / 타임아웃 / 4xx·5xx 응답 / 빈 응답 등을 모두 포괄한다.
 * {@link com.abc.behaviortracker.global.exception.GlobalExceptionHandler}가
 * {@link ErrorCode#AI_ANALYSIS_FAILED}(HTTP 502)로 매핑한다.
 *
 * <p>원인 예외(cause)는 호출부(서비스)에서 log 로 남긴다. 기존 {@code BusinessException}
 * 시그니처를 변경하지 않기 위해 cause 는 보관하지 않는다.
 */
public class AiServerException extends BusinessException {

    public AiServerException(String detailMessage) {
        super(ErrorCode.AI_ANALYSIS_FAILED, detailMessage);
    }
}
