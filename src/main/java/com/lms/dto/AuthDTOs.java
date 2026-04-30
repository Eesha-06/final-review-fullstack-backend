package com.lms.dto;

import lombok.Data;

public class AuthDTOs {

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String role; // defaults to STUDENT
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String role;
        private Long userId;
        private String name;
        private String email;

        public AuthResponse(String token, String role, Long userId, String name, String email) {
            this.token  = token;
            this.role   = role;
            this.userId = userId;
            this.name   = name;
            this.email  = email;
        }
    }
}
