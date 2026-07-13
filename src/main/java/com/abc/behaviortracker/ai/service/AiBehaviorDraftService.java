package com.abc.behaviortracker.ai.service;

import com.abc.behaviortracker.ai.domain.AiBehaviorDraft;
import com.abc.behaviortracker.ai.dto.AiAnalysisRequest;
import com.abc.behaviortracker.ai.dto.AiAnalysisResponse;
import com.abc.behaviortracker.ai.dto.AiBehaviorDraftResponse;
import com.abc.behaviortracker.ai.repository.AiBehaviorDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 분석 요청 → ai-server 호출 → 결과를 DRAFT 상태로 저장까지의 한 흐름을 담당한다.
 *
 * <p>외부 HTTP 호출({@link AiAnalysisService})과 영속화를 분리해 조합한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiBehaviorDraftService {

    private final AiAnalysisService aiAnalysisService;
    private final AiBehaviorDraftRepository draftRepository;

    /**
     * AI 분석을 요청하고, 받은 결과를 {@code DRAFT} 상태의 행동 기록 초안으로 저장한다.
     *
     * @param teacherId 초안 작성자(요청 교사)
     * @param request   ai-server 분석 요청
     * @return 저장된 초안 응답
     */
    @Transactional
    public AiBehaviorDraftResponse analyzeAndSaveDraft(Long teacherId, AiAnalysisRequest request) {
        // 1. 외부 AI 서버 호출 (실패 시 AiServerException → GlobalExceptionHandler 처리)
        AiAnalysisResponse analysis = aiAnalysisService.analyze(request);

        // 2. DRAFT 상태로 저장
        AiBehaviorDraft draft = AiBehaviorDraft.builder()
                .trackId(request.trackId())
                .studentId(request.studentId())
                .behavior(analysis.behavior())
                .confidence(analysis.confidence())
                .draftText(analysis.draftText())
                .createdBy(teacherId)
                .build();
        AiBehaviorDraft saved = draftRepository.save(draft);

        log.info("AI 행동 초안 저장(DRAFT): draftId={}, teacherId={}, trackId={}, behavior={}",
                saved.getId(), teacherId, saved.getTrackId(), saved.getBehavior());

        return AiBehaviorDraftResponse.from(saved);
    }
}
