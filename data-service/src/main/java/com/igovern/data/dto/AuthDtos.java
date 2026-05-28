package com.igovern.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "username may contain letters, digits, _ . -")
        private String username;

        @NotBlank
        @Size(min = 6, max = 100)
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest extends LoginRequest {}

    public static class TokenResponse {
        private String token;
        private String username;
        private String role;

        public TokenResponse(String token, String username, String role) {
            this.token = token;
            this.username = username;
            this.role = role;
        }
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}
