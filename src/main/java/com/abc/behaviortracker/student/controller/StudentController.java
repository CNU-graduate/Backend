package com.abc.behaviortracker.student.controller;

import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.common.PageResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import com.abc.behaviortracker.student.dto.StudentCreateRequest;
import com.abc.behaviortracker.student.dto.StudentDetailResponse;
import com.abc.behaviortracker.student.dto.StudentResponse;
import com.abc.behaviortracker.student.dto.StudentUpdateRequest;
import com.abc.behaviortracker.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Student", description = "학생 관리 API")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "학생 등록", description = "새 학생을 등록합니다. 이름+생년월일 중복 불가.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentDetailResponse> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody StudentCreateRequest request
    ) {
        StudentDetailResponse result = studentService.create(principal.teacherId(), request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "학생 목록 조회",
            description = "교사 본인이 등록한 학생들을 페이징으로 조회합니다. search 파라미터로 이름 부분 일치 검색 가능.")
    @GetMapping
    public ApiResponse<PageResponse<StudentResponse>> getList(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StudentResponse> page = studentService.getList(principal.teacherId(), search, pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @Operation(summary = "학생 상세 조회")
    @GetMapping("/{studentId}")
    public ApiResponse<StudentDetailResponse> getDetail(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId
    ) {
        StudentDetailResponse result = studentService.getDetail(principal.teacherId(), studentId);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "학생 수정", description = "PATCH 시맨틱: null 필드는 변경하지 않음. name/birthDate는 수정 불가.")
    @PatchMapping("/{studentId}")
    public ApiResponse<StudentDetailResponse> update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentDetailResponse result = studentService.update(principal.teacherId(), studentId, request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "학생 삭제", description = "Soft Delete. deleted_at만 설정되며 실제 데이터는 보존.")
    @DeleteMapping("/{studentId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long studentId
    ) {
        studentService.delete(principal.teacherId(), studentId);
        return ApiResponse.ok();
    }
}
