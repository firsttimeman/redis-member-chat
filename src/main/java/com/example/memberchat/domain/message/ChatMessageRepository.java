package com.example.memberchat.domain.message;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"room", "sender"})
    List<ChatMessage> findTop50ByRoomIdOrderBySentAtDesc(Long roomId);
}
