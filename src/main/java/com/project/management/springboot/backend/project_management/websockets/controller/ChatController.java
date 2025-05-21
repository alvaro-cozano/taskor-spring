package com.project.management.springboot.backend.project_management.websockets.controller;

import java.util.Date;
import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.project.management.springboot.backend.project_management.websockets.model.Chat;
import com.project.management.springboot.backend.project_management.websockets.model.entity.ChatMessage;
import com.project.management.springboot.backend.project_management.websockets.repository.ChatMessageRepository;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;

@RestController
@CrossOrigin(origins = "http://localhost:5173", originPatterns = "*")
public class ChatController {

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Autowired
        private ChatMessageRepository chatMessageRepository;

        @Autowired
        private UserRepository userRepository;

        @MessageMapping("/chat/{boardId}")
        public void sendMessage(@Payload Chat chatMessage,
                        @DestinationVariable Long boardId) {
                chatMessage.setTimestamp(new Date());
                chatMessage.setBoardId(boardId);

                User user = userRepository.findByEmail(chatMessage.getEmail()).orElse(null);
                byte[] profileImage = user != null ? user.getProfileImage() : null;
                String profileImageBase64 = (profileImage != null) ? Base64.getEncoder().encodeToString(profileImage)
                                : null;

                ChatMessage entity = new ChatMessage();
                entity.setEmail(chatMessage.getEmail());
                entity.setContent(chatMessage.getContent());
                entity.setTimestamp(chatMessage.getTimestamp());
                entity.setBoardId(chatMessage.getBoardId());
                entity.setProfileImage(profileImage);
                chatMessageRepository.save(entity);

                Chat chatToSend = new Chat(
                                chatMessage.getEmail(),
                                chatMessage.getContent(),
                                chatMessage.getTimestamp(),
                                chatMessage.getBoardId(),
                                profileImageBase64);
                messagingTemplate.convertAndSend("/topic/board." + boardId, chatToSend);
        }

        @GetMapping("/api/chat/{boardId}/messages")
        public List<Chat> getMessages(@PathVariable Long boardId) {
                List<ChatMessage> entities = chatMessageRepository.findByBoardIdOrderByTimestampAsc(boardId);
                return entities.stream()
                                .map(e -> new Chat(
                                                e.getEmail(),
                                                e.getContent(),
                                                e.getTimestamp(),
                                                e.getBoardId(),
                                                e.getProfileImage() != null
                                                                ? Base64.getEncoder()
                                                                                .encodeToString(e.getProfileImage())
                                                                : null))
                                .toList();
        }
}
