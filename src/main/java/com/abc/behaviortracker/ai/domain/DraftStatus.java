package com.abc.behaviortracker.ai.domain;

/**
 * AI 분석 행동 기록 초안의 상태.
 */
public enum DraftStatus {

    /** AI가 생성한 초안. 교사 확인 전. */
    DRAFT,

    /** 교사가 검토/확정한 상태. */
    CONFIRMED
}
