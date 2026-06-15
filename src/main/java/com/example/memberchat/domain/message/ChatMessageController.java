package com.example.memberchat.domain.message;

import com.example.memberchat.api.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat-rooms/{roomId}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(@PathVariable Long roomId) {
        List<ChatMessageResponse> messages = chatMessageService.viewRecentMessages(roomId);
        return ResponseEntity.ok(messages);
    }
}
