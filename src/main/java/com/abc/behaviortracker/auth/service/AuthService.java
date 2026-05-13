package com.abc.behaviortracker.auth.service;

import com.abc.behaviortracker.auth.EmailAlreadyExistsException;
import com.abc.behaviortracker.auth.InvalidCredentialsException;
import com.abc.behaviortracker.auth.dto.LoginRequest;
import com.abc.behaviortracker.auth.dto.LoginResponse;
import com.abc.behaviortracker.auth.dto.SignupRequest;
import com.abc.behaviortracker.auth.dto.TeacherDto;
import com.abc.behaviortracker.global.exception.BusinessException;
import com.abc.behaviortracker.global.exception.ErrorCode;
import com.abc.behaviortracker.global.security.JwtProperties;
import com.abc.behaviortracker.global.security.JwtProvider;
import com.abc.behaviortracker.teacher.domain.Teacher;
import com.abc.behaviortracker.teacher.domain.TeacherRole;
import com.abc.behaviortracker.teacher.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse signup(SignupRequest request) {
        if (teacherRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Teacher teacher = Teacher.builder()
                .email(request.email())
                .passwordHash(passwordHash)
                .name(request.name())
                .schoolName(request.schoolName())
                .role(TeacherRole.TEACHER)
                .build();

        Teacher saved = teacherRepository.save(teacher);

        log.info("회원가입 완료: teacherId={}, email={}", saved.getId(), saved.getEmail());

        return issueTokens(saved);
    }

    public LoginResponse login(LoginRequest request) {
        Teacher teacher = teacherRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), teacher.getPasswordHash())) {
            log.warn("비밀번호 불일치: email={}", request.email());
            throw new InvalidCredentialsException();
        }

        log.info("로그인 성공: teacherId={}, email={}", teacher.getId(), teacher.getEmail());

        return issueTokens(teacher);
    }

    public TeacherDto getMe(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        return TeacherDto.from(teacher);
    }

    private LoginResponse issueTokens(Teacher teacher) {
        String accessToken = jwtProvider.createAccessToken(
                teacher.getId(),
                teacher.getEmail(),
                teacher.getRole().name()
        );
        String refreshToken = jwtProvider.createRefreshToken(teacher.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenValiditySeconds(),
                TeacherDto.from(teacher)
        );
    }
}
