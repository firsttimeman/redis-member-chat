package com.example.memberchat.domain.chatroom;

import com.example.memberchat.api.ChatRoomResponse;
import com.example.memberchat.api.CreateChatRoomRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody @Valid CreateChatRoomRequest request) {
        ChatRoomResponse chatRoom = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        List<ChatRoomResponse> chatRooms = chatRoomService.viewChatRooms();
        return ResponseEntity.ok(chatRooms);
    }
}
