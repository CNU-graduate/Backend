package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.FrequencyItem;
import com.abc.behaviortracker.record.abc.repository.AbcRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BehaviorAnalyticsServiceTest {

    private static final Long TEACHER_ID = 1L;
    private static final Long STUDENT_ID = 10L;

    @Mock
    AbcRecordRepository abcRecordRepository;
    @Mock
    AnalyticsAccessValidator accessValidator;
    @InjectMocks
    BehaviorAnalyticsService behaviorAnalyticsService;

    @Test
    @DisplayName("가장 많이 기록된 행동을 반환한다")
    void mostFrequentBehavior() {
        when(abcRecordRepository.findBehaviorContentsByStudentId(STUDENT_ID))
                .thenReturn(List.of("자리 이탈", "자리 이탈", "소리 지르기"));

        FrequencyItem result = behaviorAnalyticsService.getMostFrequentBehavior(TEACHER_ID, STUDENT_ID);

        assertThat(result).isEqualTo(new FrequencyItem("자리 이탈", 2));
        verify(accessValidator).validateOwnership(TEACHER_ID, STUDENT_ID);
    }

    @Test
    @DisplayName("발생 횟수가 동일하면 이름 사전순으로 앞선 값을 반환한다")
    void tieBreaksByNameAscending() {
        when(abcRecordRepository.findBehaviorContentsByStudentId(STUDENT_ID))
                .thenReturn(List.of("b", "b", "a", "a"));

        FrequencyItem result = behaviorAnalyticsService.getMostFrequentBehavior(TEACHER_ID, STUDENT_ID);

        assertThat(result).isEqualTo(new FrequencyItem("a", 2));
    }

    @Test
    @DisplayName("앞뒤 공백은 trim 후 같은 값으로 집계한다")
    void trimsWhitespaceBeforeGrouping() {
        when(abcRecordRepository.findBehaviorContentsByStudentId(STUDENT_ID))
                .thenReturn(List.of("자리 이탈", " 자리 이탈 "));

        FrequencyItem result = behaviorAnalyticsService.getMostFrequentBehavior(TEACHER_ID, STUDENT_ID);

        assertThat(result).isEqualTo(new FrequencyItem("자리 이탈", 2));
    }

    @Test
    @DisplayName("행동 기록이 없으면 null을 반환한다")
    void returnsNullWhenNoBehavior() {
        when(abcRecordRepository.findBehaviorContentsByStudentId(STUDENT_ID)).thenReturn(List.of());

        FrequencyItem result = behaviorAnalyticsService.getMostFrequentBehavior(TEACHER_ID, STUDENT_ID);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("가장 많이 기록된 선행사건을 반환한다")
    void mostFrequentAntecedent() {
        when(abcRecordRepository.findAntecedentContentsByStudentId(STUDENT_ID))
                .thenReturn(List.of("과제 수행", "과제 수행", "휴식"));

        FrequencyItem result = behaviorAnalyticsService.getMostFrequentAntecedent(TEACHER_ID, STUDENT_ID);

        assertThat(result).isEqualTo(new FrequencyItem("과제 수행", 2));
        verify(accessValidator).validateOwnership(TEACHER_ID, STUDENT_ID);
    }
}
