package com.abc.behaviortracker.record.session.weather;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;

/**
 * 외부 날씨 API 호출 실패 (US-17). 네트워크 오류, 응답 파싱 실패, 4xx/5xx 모두 포함.
 */
public class WeatherFetchException extends BusinessException {

    public WeatherFetchException(String detailMessage) {
        super(ErrorCode.WEATHER_FETCH_FAILED, detailMessage);
    }

    public WeatherFetchException(String detailMessage, Throwable cause) {
        super(ErrorCode.WEATHER_FETCH_FAILED, detailMessage);
        initCause(cause);
    }
}
