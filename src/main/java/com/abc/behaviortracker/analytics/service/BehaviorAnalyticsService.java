package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.FrequencyItem;
import com.abc.behaviortracker.record.abc.repository.AbcRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 행동/선행사건 빈도 분석.
 *
 * <p>ABC 기록의 행동(content_b)·선행사건(content_a) 텍스트를 집계해 최다 항목을 계산한다.
 * 발생 횟수가 동일한 경우 이름 사전순으로 가장 앞선 값을 반환해 결과를 결정적으로 만든다.
 * 해당 기록이 전혀 없으면 {@code null}을 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BehaviorAnalyticsService {

    private final AbcRecordRepository abcRecordRepository;
    private final AnalyticsAccessValidator accessValidator;

    /** 가장 많이 기록된 행동(ABC의 B). 기록이 없으면 null. */
    public FrequencyItem getMostFrequentBehavior(Long teacherId, Long studentId) {
        accessValidator.validateOwnership(teacherId, studentId);
        return mostFrequent(abcRecordRepository.findBehaviorContentsByStudentId(studentId));
    }

    /** 가장 많이 기록된 선행사건(ABC의 A). 기록이 없으면 null. */
    public FrequencyItem getMostFrequentAntecedent(Long teacherId, Long studentId) {
        accessValidator.validateOwnership(teacherId, studentId);
        return mostFrequent(abcRecordRepository.findAntecedentContentsByStudentId(studentId));
    }

    private FrequencyItem mostFrequent(List<String> contents) {
        Map<String, Long> countByName = new LinkedHashMap<>();
        for (String content : contents) {
            if (content == null) {
                continue;
            }
            String name = content.trim();
            if (name.isEmpty()) {
                continue;
            }
            countByName.merge(name, 1L, Long::sum);
        }

        FrequencyItem top = null;
        for (Map.Entry<String, Long> entry : countByName.entrySet()) {
            if (isHigherRanked(entry, top)) {
                top = new FrequencyItem(entry.getKey(), entry.getValue());
            }
        }
        return top;
    }

    /** 후보가 현재 1위보다 우선하는가: 횟수가 더 많거나, 동일 횟수면 이름 사전순이 더 앞선 경우. */
    private boolean isHigherRanked(Map.Entry<String, Long> candidate, FrequencyItem current) {
        if (current == null) {
            return true;
        }
        if (candidate.getValue() != current.count()) {
            return candidate.getValue() > current.count();
        }
        return candidate.getKey().compareTo(current.name()) < 0;
    }
}
