package com.project.management.springboot.backend.project_management.utils.mapper;

import java.util.Base64;
import java.util.Date;

import com.project.management.springboot.backend.project_management.DTO.UserDTO;
import com.project.management.springboot.backend.project_management.entities.models.User;

public class UserMapper {

    private static byte[] convertBase64ToByteArray(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            return Base64.getDecoder().decode(base64String);
        }
        return null;
    }

    public static User toEntity(UserDTO dto) {
        return new User(
                false,
                dto.getFirst_name(),
                dto.getLast_name(),
                dto.getEmail(),
                dto.getUsername(),
                dto.getPassword(),
                false,
                new Date(),
                new Date(),
                convertBase64ToByteArray(dto.getProfileImageBase64()));
    }
}
