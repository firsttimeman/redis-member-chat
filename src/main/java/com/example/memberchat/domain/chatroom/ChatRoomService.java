package com.example.memberchat.domain.chatroom;

import com.example.memberchat.api.ChatRoomResponse;
import com.example.memberchat.api.CreateChatRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoomResponse createChatRoom(CreateChatRoomRequest request) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.name())
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomResponse.from(savedChatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> viewChatRooms() {
        return chatRoomRepository.findAll()
                .stream()
                .map(ChatRoomResponse::from)
                .toList();
    }
}
