package com.abc.behaviortracker.global.security;

public record AuthPrincipal(Long teacherId, String email, String role) {

    /** Spring Security GrantedAuthority 형식으로 변환 (예: "ROLE_TEACHER") */
    public String getAuthority() {
        return "ROLE_" + role;
    }
}
