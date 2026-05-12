package com.abc.behaviortracker.teacher.domain;

import com.abc.behaviortracker.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "teachers")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "school_name", length = 100)
    private String schoolName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private TeacherRole role;

    @Builder
    private Teacher(String email, String passwordHash, String name, String schoolName, TeacherRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.schoolName = schoolName;
        this.role = (role != null) ? role : TeacherRole.TEACHER;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateProfile(String name, String schoolName) {
        if (name != null) {
            this.name = name;
        }
        if (schoolName != null) {
            this.schoolName = schoolName;
        }
    }

    public void delete() {
        markDeleted();
    }
}
