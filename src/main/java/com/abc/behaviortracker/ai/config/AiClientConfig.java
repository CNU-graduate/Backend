package com.abc.behaviortracker.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * ai-server 호출 전용 {@link RestClient} 빈 구성.
 *
 * <p>기존 프로젝트에는 HTTP 클라이언트가 없어 신규 도입한다.
 * baseUrl/타임아웃은 {@link AiServerProperties}(=ai.server.*)에서 주입한다.
 */
@Configuration
@RequiredArgsConstructor
public class AiClientConfig {

    private final AiServerProperties properties;

    @Bean
    public RestClient aiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
