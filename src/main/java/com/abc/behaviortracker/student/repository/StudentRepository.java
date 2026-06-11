package com.abc.behaviortracker.student.repository;

import com.abc.behaviortracker.student.domain.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
            select s from Student s
            where s.teacher.id = :teacherId
              and (:search is null or :search = '' or s.name like concat('%', :search, '%'))
            """)
    Page<Student> findByTeacherIdAndNameContaining(
            @Param("teacherId") Long teacherId,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByTeacherIdAndNameAndBirthDate(Long teacherId, String name, LocalDate birthDate);

    boolean existsByTeacherIdAndNameAndBirthDateAndIdNot(
            Long teacherId,
            String name,
            LocalDate birthDate,
            Long studentId
    );
}
