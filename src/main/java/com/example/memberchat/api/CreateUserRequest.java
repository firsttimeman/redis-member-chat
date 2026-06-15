package com.example.memberchat.api;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotBlank
        String nickname
) {
}
