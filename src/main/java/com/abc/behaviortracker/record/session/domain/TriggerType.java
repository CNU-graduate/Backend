package com.abc.behaviortracker.record.session.domain;

/**
 * 세션 시작 트리거 종류.
 *
 * <p>Sprint 1에서는 {@link #SWIPE}와 {@link #MANUAL}만 실제로 사용된다.
 * {@link #NOISE}는 Sprint 3에서 자동 데시벨 감지 기능과 함께 활성화될 예정이다.
 */
public enum TriggerType {

    /** 수동 스와이프 트리거 (US-05, US-13). */
    SWIPE,

    /** 자동 데시벨 트리거 (US-14). Sprint 3에서 활성화 예정. */
    NOISE,

    /** 일반 버튼 클릭 등 기타 수동 시작. */
    MANUAL
}
