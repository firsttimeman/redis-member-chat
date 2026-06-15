package com.example.memberchat.api;

import com.example.memberchat.domain.chatroom.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        String name,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getCreatedAt()
        );
    }
}
