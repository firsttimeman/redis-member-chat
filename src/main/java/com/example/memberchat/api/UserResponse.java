package com.example.memberchat.api;

import com.example.memberchat.domain.user.User;

public record UserResponse(
        Long userId,
        String username,
        String nickname
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}
