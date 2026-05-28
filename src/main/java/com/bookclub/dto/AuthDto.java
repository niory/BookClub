package com.bookclub.dto;

import com.bookclub.entity.User.Role;
import jakarta.validation.constraints.*;
import lombok.*;

// ─── Auth ────────────────────────────────────────────────────────────────────

public class AuthDto {

    @Getter @Setter
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 50)
        private String username;
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6)
        private String password;
        private Role role = Role.READER;
    }

    @Getter @Setter
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Getter @AllArgsConstructor
    public static class TokenResponse {
        private String token;
        private String username;
        private String role;
    }
}
