package com.project.management.springboot.backend.project_management.websockets.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.project.management.springboot.backend.project_management.websockets.model.entity.ChatMessage;

import java.util.Date;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String content;
    private Date timestamp;
    private Long boardId;

    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;
}