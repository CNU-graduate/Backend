package com.abc.behaviortracker.student.service;

import com.abc.behaviortracker.student.StudentAlreadyExistsException;
import com.abc.behaviortracker.student.dto.StudentCreateRequest;
import com.abc.behaviortracker.student.dto.StudentDetailResponse;
import com.abc.behaviortracker.student.dto.StudentUpdateRequest;
import com.abc.behaviortracker.student.repository.StudentRepository;
import com.abc.behaviortracker.teacher.domain.Teacher;
import com.abc.behaviortracker.teacher.repository.TeacherRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void updateChangesAllEditableStudentFields() {
        Teacher teacher = saveTeacher("teacher-update@example.com");
        StudentDetailResponse created = createStudent(
                teacher.getId(), "Old Name", LocalDate.of(2015, 1, 2)
        );

        StudentDetailResponse updated = studentService.update(
                teacher.getId(),
                created.studentId(),
                new StudentUpdateRequest(
                        "New Name",
                        5,
                        LocalDate.of(2014, 3, 4),
                        "",
                        Map.of("note", "updated")
                )
        );

        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.grade()).isEqualTo(5);
        assertThat(updated.birthDate()).isEqualTo(LocalDate.of(2014, 3, 4));
        assertThat(updated.iepSummary()).isEmpty();
        assertThat(updated.metadata()).containsEntry("note", "updated");

        entityManager.flush();
        entityManager.clear();

        assertThat(studentRepository.findById(updated.studentId()))
                .get()
                .extracting(student -> student.getMetadata().get("note"))
                .isEqualTo("updated");
    }

    @Test
    void updateRejectsAnotherStudentWithSameNameAndBirthDate() {
        Teacher teacher = saveTeacher("teacher-duplicate@example.com");
        StudentDetailResponse first = createStudent(
                teacher.getId(), "First Student", LocalDate.of(2015, 1, 2)
        );
        StudentDetailResponse second = createStudent(
                teacher.getId(), "Second Student", LocalDate.of(2015, 5, 6)
        );

        assertThatThrownBy(() -> studentService.update(
                teacher.getId(),
                second.studentId(),
                new StudentUpdateRequest(
                        first.name(),
                        null,
                        first.birthDate(),
                        null,
                        null
                )
        )).isInstanceOf(StudentAlreadyExistsException.class);
    }

    private Teacher saveTeacher(String email) {
        return teacherRepository.save(Teacher.builder()
                .email(email)
                .passwordHash("password-hash")
                .name("Teacher")
                .schoolName("School")
                .build());
    }

    private StudentDetailResponse createStudent(Long teacherId, String name, LocalDate birthDate) {
        return studentService.create(
                teacherId,
                new StudentCreateRequest(name, 3, birthDate, "Initial memo", Map.of())
        );
    }
}
