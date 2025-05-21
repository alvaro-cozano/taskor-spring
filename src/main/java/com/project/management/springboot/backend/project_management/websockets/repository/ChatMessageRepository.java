package com.project.management.springboot.backend.project_management.websockets.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.management.springboot.backend.project_management.websockets.model.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByBoardIdOrderByTimestampAsc(Long boardId);
}