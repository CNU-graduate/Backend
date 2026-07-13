package com.abc.behaviortracker.analytics.service;

import com.abc.behaviortracker.analytics.dto.FrequencyItemResponse;
import com.abc.behaviortracker.analytics.dto.PeriodSummaryResponse;
import com.abc.behaviortracker.analytics.dto.StatusCountResponse;
import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.record.abc.domain.AbcRecord;
import com.abc.behaviortracker.record.abc.repository.AbcRecordRepository;
import com.abc.behaviortracker.record.session.domain.RecordSession;
import com.abc.behaviortracker.record.session.domain.SessionStatus;
import com.abc.behaviortracker.record.session.domain.TriggerType;
import com.abc.behaviortracker.record.session.repository.RecordSessionRepository;
import com.abc.behaviortracker.student.StudentNotFoundException;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.student.repository.StudentRepository;
import com.abc.behaviortracker.teacher.domain.Teacher;
import com.abc.behaviortracker.teacher.repository.TeacherRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BehaviorAnalyticsServiceIntegrationTest {

    @Autowired
    private BehaviorAnalyticsService behaviorAnalyticsService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RecordSessionRepository sessionRepository;

    @Autowired
    private AbcRecordRepository abcRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void countsSameBehaviorAfterTrimAndSortsByCountDesc() {
        Teacher teacher = saveTeacher("teacher-freq1@example.com");
        Student student = saveStudent(teacher, "Student A");

        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "이탈행동");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), " 이탈행동 ");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "울음");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).containsExactly(
                new FrequencyItemResponse("이탈행동", 2L),
                new FrequencyItemResponse("울음", 1L)
        );
    }

    @Test
    void breaksCountTieByLabelAscending() {
        Teacher teacher = saveTeacher("teacher-freq2@example.com");
        Student student = saveStudent(teacher, "Student B");

        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "나");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "가");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).extracting(FrequencyItemResponse::label)
                .containsExactly("가", "나");
    }

    @Test
    void excludesNullAndBlankBehavior() {
        Teacher teacher = saveTeacher("teacher-freq3@example.com");
        Student student = saveStudent(teacher, "Student C");

        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), null);
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "   ");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "실제행동");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).containsExactly(new FrequencyItemResponse("실제행동", 1L));
    }

    @Test
    void excludesOtherStudentsRecords() {
        Teacher teacher = saveTeacher("teacher-freq4@example.com");
        Student studentA = saveStudent(teacher, "Student D");
        Student studentB = saveStudent(teacher, "Student E");

        saveAbcRecord(saveSessionAt(teacher, studentA, Instant.now()), "행동A");
        saveAbcRecord(saveSessionAt(teacher, studentB, Instant.now()), "행동B");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(teacher.getId(), studentA.getId(), null, null);

        assertThat(result).containsExactly(new FrequencyItemResponse("행동A", 1L));
    }

    @Test
    void filtersByStartedAtPeriodInclusiveBounds() {
        Teacher teacher = saveTeacher("teacher-freq5@example.com");
        Student student = saveStudent(teacher, "Student F");

        Instant inside = Instant.parse("2026-06-01T00:00:00Z");
        Instant outside = Instant.parse("2026-01-01T00:00:00Z");

        saveAbcRecord(saveSessionAt(teacher, student, inside), "포함");
        saveAbcRecord(saveSessionAt(teacher, student, outside), "제외");

        List<FrequencyItemResponse> result = behaviorAnalyticsService.getBehaviorFrequency(
                teacher.getId(), student.getId(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-06-30T23:59:59Z")
        );

        assertThat(result).containsExactly(new FrequencyItemResponse("포함", 1L));
    }

    @Test
    void rejectsFromAfterTo() {
        Teacher teacher = saveTeacher("teacher-freq6@example.com");
        Student student = saveStudent(teacher, "Student G");

        assertThatThrownBy(() -> behaviorAnalyticsService.getBehaviorFrequency(
                teacher.getId(), student.getId(),
                Instant.parse("2026-06-30T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void rejectsAccessByNonOwningTeacher() {
        Teacher owner = saveTeacher("teacher-freq7-owner@example.com");
        Teacher stranger = saveTeacher("teacher-freq7-stranger@example.com");
        Student student = saveStudent(owner, "Student H");

        assertThatThrownBy(() -> behaviorAnalyticsService.getBehaviorFrequency(
                stranger.getId(), student.getId(), null, null))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void rejectsUnknownStudentId() {
        Teacher teacher = saveTeacher("teacher-freq8@example.com");

        assertThatThrownBy(() -> behaviorAnalyticsService.getBehaviorFrequency(
                teacher.getId(), -1L, null, null))
                .isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void returnsEmptyListWhenNoRecords() {
        Teacher teacher = saveTeacher("teacher-freq9@example.com");
        Student student = saveStudent(teacher, "Student I");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getBehaviorFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void countsSameAntecedentAfterTrimAndSortsByCountDesc() {
        Teacher teacher = saveTeacher("teacher-ant1@example.com");
        Student student = saveStudent(teacher, "Student J");

        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "수학 시간", "행동1");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), " 수학 시간 ", "행동2");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "체육 시간", "행동3");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getAntecedentFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).containsExactly(
                new FrequencyItemResponse("수학 시간", 2L),
                new FrequencyItemResponse("체육 시간", 1L)
        );
    }

    @Test
    void excludesNullAndBlankAntecedent() {
        Teacher teacher = saveTeacher("teacher-ant2@example.com");
        Student student = saveStudent(teacher, "Student K");

        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), null, "행동");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "   ", "행동");
        saveAbcRecord(saveSessionAt(teacher, student, Instant.now()), "실제선행사건", "행동");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getAntecedentFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).containsExactly(new FrequencyItemResponse("실제선행사건", 1L));
    }

    @Test
    void antecedentFrequencyFiltersByStartedAtPeriod() {
        Teacher teacher = saveTeacher("teacher-ant3@example.com");
        Student student = saveStudent(teacher, "Student L");

        Instant inside = Instant.parse("2026-06-01T00:00:00Z");
        Instant outside = Instant.parse("2026-01-01T00:00:00Z");

        saveAbcRecord(saveSessionAt(teacher, student, inside), "포함", "행동");
        saveAbcRecord(saveSessionAt(teacher, student, outside), "제외", "행동");

        List<FrequencyItemResponse> result = behaviorAnalyticsService.getAntecedentFrequency(
                teacher.getId(), student.getId(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-06-30T23:59:59Z")
        );

        assertThat(result).containsExactly(new FrequencyItemResponse("포함", 1L));
    }

    @Test
    void antecedentFrequencyRejectsFromAfterTo() {
        Teacher teacher = saveTeacher("teacher-ant4@example.com");
        Student student = saveStudent(teacher, "Student M");

        assertThatThrownBy(() -> behaviorAnalyticsService.getAntecedentFrequency(
                teacher.getId(), student.getId(),
                Instant.parse("2026-06-30T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void antecedentFrequencyRejectsAccessByNonOwningTeacher() {
        Teacher owner = saveTeacher("teacher-ant5-owner@example.com");
        Teacher stranger = saveTeacher("teacher-ant5-stranger@example.com");
        Student student = saveStudent(owner, "Student N");

        assertThatThrownBy(() -> behaviorAnalyticsService.getAntecedentFrequency(
                stranger.getId(), student.getId(), null, null))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void antecedentFrequencyReturnsEmptyListWhenNoRecords() {
        Teacher teacher = saveTeacher("teacher-ant6@example.com");
        Student student = saveStudent(teacher, "Student O");

        List<FrequencyItemResponse> result =
                behaviorAnalyticsService.getAntecedentFrequency(teacher.getId(), student.getId(), null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void periodSummaryCountsSessionsByStatusAndSumsToTotal() {
        Teacher teacher = saveTeacher("teacher-sum1@example.com");
        Student student = saveStudent(teacher, "Student P");

        saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.COMPLETED);
        saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.COMPLETED);
        saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.ABANDONED);
        saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.RECORDING);

        PeriodSummaryResponse result =
                behaviorAnalyticsService.getPeriodSummary(teacher.getId(), student.getId(), null, null);

        assertThat(result.totalSessionCount()).isEqualTo(4);
        assertThat(result.statusCounts()).containsExactlyInAnyOrder(
                new StatusCountResponse(SessionStatus.COMPLETED, 2L),
                new StatusCountResponse(SessionStatus.ABANDONED, 1L),
                new StatusCountResponse(SessionStatus.RECORDING, 1L)
        );
        assertThat(result.statusCounts().stream().mapToLong(StatusCountResponse::count).sum())
                .isEqualTo(result.totalSessionCount());
    }

    @Test
    void periodSummaryCountsAbcRecordsWithinPeriod() {
        Teacher teacher = saveTeacher("teacher-sum2@example.com");
        Student student = saveStudent(teacher, "Student Q");

        RecordSession s1 = saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.COMPLETED);
        saveAbcRecord(s1, "행동1");
        RecordSession s2 = saveSessionWithStatus(teacher, student, Instant.now(), SessionStatus.ENDED);
        saveAbcRecord(s2, null);

        PeriodSummaryResponse result =
                behaviorAnalyticsService.getPeriodSummary(teacher.getId(), student.getId(), null, null);

        assertThat(result.totalAbcRecordCount()).isEqualTo(2);
    }

    @Test
    void periodSummaryFiltersByStartedAtPeriod() {
        Teacher teacher = saveTeacher("teacher-sum3@example.com");
        Student student = saveStudent(teacher, "Student R");

        Instant inside = Instant.parse("2026-06-15T00:00:00Z");
        Instant outside = Instant.parse("2026-01-01T00:00:00Z");

        saveSessionWithStatus(teacher, student, inside, SessionStatus.COMPLETED);
        saveSessionWithStatus(teacher, student, outside, SessionStatus.COMPLETED);

        PeriodSummaryResponse result = behaviorAnalyticsService.getPeriodSummary(
                teacher.getId(), student.getId(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-06-30T23:59:59Z")
        );

        assertThat(result.totalSessionCount()).isEqualTo(1);
        assertThat(result.statusCounts()).containsExactly(new StatusCountResponse(SessionStatus.COMPLETED, 1L));
    }

    @Test
    void periodSummaryReturnsZeroCountsWhenNoSessions() {
        Teacher teacher = saveTeacher("teacher-sum4@example.com");
        Student student = saveStudent(teacher, "Student S");

        PeriodSummaryResponse result =
                behaviorAnalyticsService.getPeriodSummary(teacher.getId(), student.getId(), null, null);

        assertThat(result.totalSessionCount()).isZero();
        assertThat(result.totalAbcRecordCount()).isZero();
        assertThat(result.statusCounts()).isEmpty();
    }

    @Test
    void periodSummaryRejectsFromAfterTo() {
        Teacher teacher = saveTeacher("teacher-sum5@example.com");
        Student student = saveStudent(teacher, "Student T");

        assertThatThrownBy(() -> behaviorAnalyticsService.getPeriodSummary(
                teacher.getId(), student.getId(),
                Instant.parse("2026-06-30T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void periodSummaryRejectsAccessByNonOwningTeacher() {
        Teacher owner = saveTeacher("teacher-sum6-owner@example.com");
        Teacher stranger = saveTeacher("teacher-sum6-stranger@example.com");
        Student student = saveStudent(owner, "Student U");

        assertThatThrownBy(() -> behaviorAnalyticsService.getPeriodSummary(
                stranger.getId(), student.getId(), null, null))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    @Test
    void periodSummaryRejectsUnknownStudentId() {
        Teacher teacher = saveTeacher("teacher-sum7@example.com");

        assertThatThrownBy(() -> behaviorAnalyticsService.getPeriodSummary(
                teacher.getId(), -1L, null, null))
                .isInstanceOf(StudentNotFoundException.class);
    }

    private Teacher saveTeacher(String email) {
        return teacherRepository.save(Teacher.builder()
                .email(email)
                .passwordHash("password-hash")
                .name("Teacher")
                .schoolName("School")
                .build());
    }

    private Student saveStudent(Teacher teacher, String name) {
        return studentRepository.save(Student.builder()
                .teacher(teacher)
                .name(name)
                .grade(1)
                .build());
    }

    private RecordSession saveSessionAt(Teacher teacher, Student student, Instant startedAt) {
        RecordSession session = sessionRepository.save(RecordSession.builder()
                .teacher(teacher)
                .student(student)
                .triggerType(TriggerType.MANUAL)
                .mediaAssisted(false)
                .build());

        entityManager.createQuery("UPDATE RecordSession s SET s.startedAt = :startedAt WHERE s.id = :id")
                .setParameter("startedAt", startedAt)
                .setParameter("id", session.getId())
                .executeUpdate();
        entityManager.refresh(session);

        return session;
    }

    private RecordSession saveSessionWithStatus(
            Teacher teacher, Student student, Instant startedAt, SessionStatus status
    ) {
        RecordSession session = saveSessionAt(teacher, student, startedAt);

        switch (status) {
            case RECORDING -> { }
            case ENDED -> session.end();
            case COMPLETED -> {
                session.end();
                session.markCompleted();
            }
            case INCOMPLETE -> {
                session.end();
                session.markIncomplete();
            }
            case ABANDONED -> session.abandon(null);
            case INIT -> throw new IllegalArgumentException("INIT 상태는 테스트 픽스처로 생성하지 않습니다");
        }

        return sessionRepository.save(session);
    }

    private AbcRecord saveAbcRecord(RecordSession session, String contentB) {
        return saveAbcRecord(session, "선행사건", contentB);
    }

    private AbcRecord saveAbcRecord(RecordSession session, String contentA, String contentB) {
        return abcRecordRepository.save(AbcRecord.builder()
                .session(session)
                .contentA(contentA)
                .contentB(contentB)
                .contentC("결과")
                .createdBy(session.getTeacher().getId())
                .build());
    }
}