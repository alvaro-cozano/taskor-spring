package com.project.management.springboot.backend.project_management.services.user;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.entities.models.User;

import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface UserService {

    List<User> findAll();

    List<UserReferenceDTO> findAllEmails();

    User save(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    String findEmailByUsername(String username);

    User updateProfile(Long id, User user);

    PasswordEncoder getPasswordEncoder();

    void sendVerificationEmail(User user);

    void requestEmailChange(String currentUsername, String newEmail) throws MessagingException, IOException;

    boolean confirmEmailChange(String token);

    Optional<User> findByEmail(String email);

    void addRoleToUser(Long userId, String roleName);

    void removeRoleFromUser(Long userId, String roleName);
}
