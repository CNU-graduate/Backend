package com.abc.behaviortracker.ai.service;

import com.abc.behaviortracker.ai.config.AiServerProperties;
import com.abc.behaviortracker.ai.dto.AiAnalysisRequest;
import com.abc.behaviortracker.ai.dto.AiAnalysisResponse;
import com.abc.behaviortracker.ai.exception.AiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

/**
 * 외부 AI 분석 서버(ai-server)의 /analyze 엔드포인트를 호출하는 서비스.
 *
 * <p>ai-server 스펙이 아직 고정되지 않았으므로, 더미 URL(ai.server.base-url, 기본
 * http://localhost:8000) 기준으로 동작 구조만 잡아 둔다. 스펙 변경 시
 * {@link AiAnalysisRequest}/{@link AiAnalysisResponse}와 이 클래스만 수정하면 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final RestClient aiRestClient;
    private final AiServerProperties properties;

    /**
     * ai-server 에 분석을 요청하고 결과를 받아온다.
     *
     * @throws AiServerException 연결 실패 / 타임아웃 / 4xx·5xx / 빈 응답 등 호출 실패 시
     */
    public AiAnalysisResponse analyze(AiAnalysisRequest request) {
        try {
            AiAnalysisResponse response = aiRestClient.post()
                    .uri(properties.getAnalyzePath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiAnalysisResponse.class);

            if (response == null) {
                log.error("AI 분석 서버가 빈 응답 반환: trackId={}, studentId={}",
                        request.trackId(), request.studentId());
                throw new AiServerException("AI 분석 서버가 빈 응답을 반환했습니다");
            }

            log.info("AI 분석 완료: trackId={}, behavior={}, confidence={}",
                    request.trackId(), response.behavior(), response.confidence());
            return response;

        } catch (RestClientResponseException e) {
            // 4xx / 5xx 응답
            log.error("AI 분석 서버 오류 응답: status={}, trackId={}, body={}",
                    e.getStatusCode(), request.trackId(), e.getResponseBodyAsString(), e);
            throw new AiServerException("AI 분석 서버가 오류 응답을 반환했습니다: " + e.getStatusCode());

        } catch (ResourceAccessException e) {
            // 연결 실패 / 타임아웃
            log.error("AI 분석 서버 연결 실패/타임아웃: trackId={}", request.trackId(), e);
            throw new AiServerException("AI 분석 서버에 연결할 수 없습니다");

        } catch (RestClientException e) {
            // 직렬화/역직렬화 등 기타 클라이언트 오류
            log.error("AI 분석 서버 호출 중 오류: trackId={}", request.trackId(), e);
            throw new AiServerException("AI 분석 서버 호출 중 오류가 발생했습니다");
        }
    }
}
