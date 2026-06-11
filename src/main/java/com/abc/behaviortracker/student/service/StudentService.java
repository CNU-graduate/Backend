package com.abc.behaviortracker.student.service;

import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.exception.ForbiddenAccessException;
import com.abc.behaviortracker.student.StudentAlreadyExistsException;
import com.abc.behaviortracker.student.StudentNotFoundException;
import com.abc.behaviortracker.student.domain.Student;
import com.abc.behaviortracker.student.dto.StudentCreateRequest;
import com.abc.behaviortracker.student.dto.StudentDetailResponse;
import com.abc.behaviortracker.student.dto.StudentResponse;
import com.abc.behaviortracker.student.dto.StudentUpdateRequest;
import com.abc.behaviortracker.student.repository.StudentRepository;
import com.abc.behaviortracker.teacher.domain.Teacher;
import com.abc.behaviortracker.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public StudentDetailResponse create(Long teacherId, StudentCreateRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        if (studentRepository.existsByTeacherIdAndNameAndBirthDate(
                teacherId, request.name(), request.birthDate())) {
            throw new StudentAlreadyExistsException(request.name());
        }

        Student student = Student.builder()
                .teacher(teacher)
                .name(request.name())
                .grade(request.grade())
                .birthDate(request.birthDate())
                .iepSummary(request.iepSummary())
                .metadata(request.metadata())
                .build();

        Student saved = studentRepository.save(student);

        log.info("학생 등록 완료: studentId={}, teacherId={}, name={}",
                saved.getId(), teacherId, saved.getName());

        return StudentDetailResponse.from(saved);
    }

    public Page<StudentResponse> getList(Long teacherId, String search, Pageable pageable) {
        return studentRepository.findByTeacherIdAndNameContaining(teacherId, search, pageable)
                .map(StudentResponse::from);
    }

    public StudentDetailResponse getDetail(Long teacherId, Long studentId) {
        Student student = findStudentOwnedBy(teacherId, studentId);
        return StudentDetailResponse.from(student);
    }

    @Transactional
    public StudentDetailResponse update(Long teacherId, Long studentId, StudentUpdateRequest request) {
        Student student = findStudentOwnedBy(teacherId, studentId);
        String name = request.name() != null ? request.name().trim() : student.getName();
        LocalDate birthDate = request.birthDate() != null
                ? request.birthDate()
                : student.getBirthDate();

        if (studentRepository.existsByTeacherIdAndNameAndBirthDateAndIdNot(
                teacherId, name, birthDate, studentId)) {
            throw new StudentAlreadyExistsException(name);
        }

        student.update(
                name,
                request.grade(),
                birthDate,
                request.iepSummary(),
                request.metadata()
        );

        log.info("학생 수정 완료: studentId={}, teacherId={}", studentId, teacherId);

        return StudentDetailResponse.from(student);
    }

    @Transactional
    public void delete(Long teacherId, Long studentId) {
        Student student = findStudentOwnedBy(teacherId, studentId);

        student.delete();

        log.info("학생 삭제 완료: studentId={}, teacherId={}", studentId, teacherId);
    }

    private Student findStudentOwnedBy(Long teacherId, Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        if (!student.isOwnedBy(teacherId)) {
            log.warn("권한 없는 학생 접근 시도: studentId={}, teacherId={}", studentId, teacherId);
            throw new ForbiddenAccessException();
        }

        return student;
    }
}
