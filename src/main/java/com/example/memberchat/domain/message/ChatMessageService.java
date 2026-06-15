package com.example.memberchat.domain.message;

import com.example.memberchat.api.ChatMessageResponse;
import com.example.memberchat.api.SendChatMessageRequest;
import com.example.memberchat.domain.chatroom.ChatRoom;
import com.example.memberchat.domain.chatroom.ChatRoomRepository;
import com.example.memberchat.domain.user.User;
import com.example.memberchat.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> viewRecentMessages(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found");
        }

        return chatMessageRepository.findTop50ByRoomIdOrderBySentAtDesc(roomId)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse saveMessage(SendChatMessageRequest request) {

        ChatRoom room = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        User user = userRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatMessage build = ChatMessage.builder()
                .room(room)
                .sender(user)
                .content(request.content())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(build);

        return ChatMessageResponse.from(savedMessage);
    }
}
