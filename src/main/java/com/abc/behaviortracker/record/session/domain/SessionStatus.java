package com.abc.behaviortracker.record.session.domain;

public enum SessionStatus {

    /** 세션 생성 직후 (Sprint 1에서는 사실상 미사용, RECORDING으로 즉시 전이). */
    INIT,

    /** 행동 기록 중 (US-05). */
    RECORDING,

    /** 행동 종료, ABC 입력 대기 (US-08). */
    ENDED,

    /** ABC 입력 완료 (US-09). */
    COMPLETED,

    /** 종료됐으나 ABC 일부만 입력됨 (US-09 AC-02). */
    INCOMPLETE,

    /** 비정상 종료 (앱 강제 종료 등, US-06 A2). */
    ABANDONED;

    public boolean isActive() {
        return this == INIT || this == RECORDING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == INCOMPLETE || this == ABANDONED;
    }
}
