package com.abc.behaviortracker.student.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import com.abc.behaviortracker.teacher.domain.Teacher;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name = "students",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_students_teacher_name_birth",
                columnNames = {"teacher_id", "name", "birth_date"}
        ))
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "iep_summary", columnDefinition = "TEXT")
    private String iepSummary;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @Builder
    private Student(Teacher teacher, String name, Integer grade, LocalDate birthDate,
                    String iepSummary, Map<String, Object> metadata) {
        this.teacher = teacher;
        this.name = name;
        this.grade = grade;
        this.birthDate = birthDate;
        this.iepSummary = iepSummary;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public void update(Integer grade, String iepSummary, Map<String, Object> metadata) {
        if (grade != null) {
            this.grade = grade;
        }
        if (iepSummary != null) {
            this.iepSummary = iepSummary;
        }
        if (metadata != null) {
            this.metadata = new HashMap<>(metadata);
        }
    }

    public void delete() {
        markDeleted();
    }

    public boolean isOwnedBy(Long teacherId) {
        return this.teacher != null && this.teacher.getId().equals(teacherId);
    }
}
