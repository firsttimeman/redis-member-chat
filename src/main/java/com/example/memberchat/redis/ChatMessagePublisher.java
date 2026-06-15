package com.example.memberchat.redis;

import com.example.memberchat.api.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final RedisTemplate<String, ChatMessageResponse> redisTemplate;

    @Value("${chat.redis.topic}")
    private String topic;

    public void publish(ChatMessageResponse message) {
        redisTemplate.convertAndSend(topic, message);
    }
}
