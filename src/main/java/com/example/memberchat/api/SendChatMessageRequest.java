package com.example.memberchat.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendChatMessageRequest(
        @NotNull
        Long roomId,

        @NotNull
        Long senderId,

        @NotBlank
        String content
) {
}
