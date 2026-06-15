package com.example.memberchat.api;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(
        @NotBlank
        String name
) {
}
