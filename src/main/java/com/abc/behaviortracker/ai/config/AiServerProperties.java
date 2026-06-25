package com.abc.behaviortracker.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 외부 AI 분석 서버(ai-server) 연동 설정.
 *
 * <p>application.yaml 의 {@code ai.server.*} 키로 외부 주입한다. 하드코딩 금지.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.server")
public class AiServerProperties {

    /** ai-server 기본 URL (예: http://localhost:8000). */
    private String baseUrl;

    /** 분석 엔드포인트 경로. baseUrl 에 이어 붙는다. */
    private String analyzePath = "/analyze";

    /** 연결 타임아웃 (기본 5초). */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 응답 읽기 타임아웃 (기본 5초). */
    private Duration readTimeout = Duration.ofSeconds(5);
}
