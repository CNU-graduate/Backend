package com.abc.behaviortracker.auth.controller;

import com.abc.behaviortracker.auth.dto.LoginRequest;
import com.abc.behaviortracker.auth.dto.LoginResponse;
import com.abc.behaviortracker.auth.dto.RefreshRequest;
import com.abc.behaviortracker.auth.dto.SignupRequest;
import com.abc.behaviortracker.auth.dto.TeacherDto;
import com.abc.behaviortracker.auth.service.AuthService;
import com.abc.behaviortracker.global.common.ApiResponse;
import com.abc.behaviortracker.global.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새 교사 계정을 생성하고 JWT를 발급합니다. (가입 후 자동 로그인)")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LoginResponse> signup(@Valid @RequestBody SignupRequest request) {
        LoginResponse result = authService.signup(request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 인증 후 JWT를 발급합니다.")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ApiResponse.ok(result);
    }

    @Operation(summary = "내 정보 조회", description = "JWT로 인증된 현재 교사 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<TeacherDto> getMe(@AuthenticationPrincipal AuthPrincipal principal) {
        TeacherDto result = authService.getMe(principal.teacherId());
        return ApiResponse.ok(result);
    }

    @Operation(summary = "토큰 갱신",
            description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse result = authService.refresh(request.refreshToken());
        return ApiResponse.ok(result);
    }
}
