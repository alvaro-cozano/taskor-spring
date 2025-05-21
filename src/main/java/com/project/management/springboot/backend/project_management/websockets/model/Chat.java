package com.project.management.springboot.backend.project_management.websockets.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private String email;
    private String content;
    private Date timestamp;
    private Long boardId;
    private String profileImage;
}
