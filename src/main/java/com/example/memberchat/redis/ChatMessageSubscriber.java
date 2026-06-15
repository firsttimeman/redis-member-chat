package com.example.memberchat.redis;

import com.example.memberchat.api.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageResponse response = objectMapper.readValue(
                    message.getBody(),
                    ChatMessageResponse.class
            );

            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + response.roomId(),
                    response
            );

        } catch (Exception e) {
            throw new RuntimeException("failed to send message", e);
        }
    }
}
