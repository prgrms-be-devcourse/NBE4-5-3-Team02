package com.snackoverflow.toolgether.domain.chat.repository;

import com.snackoverflow.toolgether.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
}
