package com.abc.behaviortracker.record.session.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.teacher.domain.Teacher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.Instant;

@Getter
@Entity
@Table(name = "record_sessions",
        indexes = {
                @Index(name = "idx_sessions_student_started", columnList = "student_id, started_at DESC"),
                @Index(name = "idx_sessions_teacher_status", columnList = "teacher_id, status")
        })
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private TriggerType triggerType;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "media_assisted", nullable = false)
    private boolean mediaAssisted;

    @Builder
    private RecordSession(Teacher teacher, Student student, TriggerType triggerType, boolean mediaAssisted) {
        this.teacher = teacher;
        this.student = student;
        this.triggerType = triggerType != null ? triggerType : TriggerType.MANUAL;
        this.mediaAssisted = mediaAssisted;
        this.status = SessionStatus.RECORDING;
        this.startedAt = Instant.now();
    }

    public void end() {
        if (this.status != SessionStatus.RECORDING) {
            throw new IllegalStateException(
                    "RECORDING 상태에서만 종료할 수 있습니다. 현재 상태: " + this.status
            );
        }
        this.endedAt = Instant.now();
        this.status = SessionStatus.ENDED;
    }

    public void markCompleted() {
        if (this.status == SessionStatus.COMPLETED) {
            return;
        }
        if (this.status != SessionStatus.ENDED && this.status != SessionStatus.INCOMPLETE) {
            throw new IllegalStateException(
                    "ENDED 또는 INCOMPLETE 상태에서만 완료 처리 가능합니다. 현재 상태: " + this.status
            );
        }
        this.status = SessionStatus.COMPLETED;
    }

    public void markIncomplete() {
        if (this.status == SessionStatus.INCOMPLETE) {
            return;
        }
        if (this.status != SessionStatus.ENDED && this.status != SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "ENDED 또는 COMPLETED 상태에서만 미완료 처리 가능합니다. 현재 상태: " + this.status
            );
        }
        this.status = SessionStatus.INCOMPLETE;
    }

    public void abandon(Instant lastValidAt) {
        if (this.status.isTerminal()) {
            return;
        }
        this.endedAt = lastValidAt != null ? lastValidAt : Instant.now();
        this.status = SessionStatus.ABANDONED;
    }

    public void delete() {
        markDeleted();
    }

    public Duration getDuration() {
        if (endedAt == null) {
            return null;
        }
        return Duration.between(startedAt, endedAt);
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.teacher != null && this.teacher.getId().equals(teacherId);
    }
}
