package com.example.memberchat.websocket;

import com.example.memberchat.api.ChatMessageResponse;
import com.example.memberchat.api.SendChatMessageRequest;
import com.example.memberchat.domain.message.ChatMessageService;
import com.example.memberchat.redis.ChatMessagePublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatMessagePublisher chatMessagePublisher;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendChatMessageRequest request) {
        ChatMessageResponse response = chatMessageService.saveMessage(request);
        chatMessagePublisher.publish(response);
    }
}
