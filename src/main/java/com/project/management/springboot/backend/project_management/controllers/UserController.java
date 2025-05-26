package com.project.management.springboot.backend.project_management.controllers;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.project.management.springboot.backend.project_management.DTO.UserDTO;
import com.project.management.springboot.backend.project_management.DTO.UserProfileDTO;
import com.project.management.springboot.backend.project_management.DTO.UserReferenceDTO;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.services.user.UserService;
import com.project.management.springboot.backend.project_management.utils.mapper.UserMapper;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@CrossOrigin(origins = "${app.front-url}", originPatterns = "*")
@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService service;

    @Value("${app.front-url}")
    private String frontendBaseUrl;

    @GetMapping
    public List<User> list() {
        return service.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserDTO userDto, BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }

        User user = UserMapper.toEntity(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDto, BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }
        if (service.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("email", "El email ya está registrado"));
        }

        if (service.existsByUsername(userDto.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("username", "El nombre de usuario ya existe"));
        }
        User user = UserMapper.toEntity(userDto);
        User savedUser = service.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalUser = service.findByUsername(username);

        return optionalUser.map(user -> {
            UserProfileDTO dto = new UserProfileDTO();
            dto.setFirst_name(user.getFirst_name());
            dto.setLast_name(user.getLast_name());
            dto.setEmail(user.getEmail());
            dto.setUsername(user.getUsername());

            if (user.getProfileImage() != null) {
                dto.setProfileImage(Base64.getEncoder().encodeToString(user.getProfileImage()));
            }

            return ResponseEntity.ok(dto);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UserProfileDTO dto,
            Authentication authentication) {

        Optional<User> optionalUser = service.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User userToUpdate = optionalUser.get();

        if (!userToUpdate.getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No puedes editar otro perfil"));
        }

        if ((dto.getPassword1() != null || dto.getPassword2() != null)) {
            if (dto.getPassword1() == null || !dto.getPassword1().equals(dto.getPassword2())) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("password", "Las contraseñas no coinciden o una está vacía."));
            }
        }

        User userForImmediateUpdate = new User();
        userForImmediateUpdate.setFirst_name(dto.getFirst_name());
        userForImmediateUpdate.setLast_name(dto.getLast_name());
        userForImmediateUpdate.setUsername(dto.getUsername());

        if (dto.getPassword1() != null && !dto.getPassword1().isBlank()) {
            userForImmediateUpdate.setPassword(dto.getPassword1());
        }

        if (dto.getProfileImageBase64() != null && !dto.getProfileImageBase64().isBlank()) {
            try {
                byte[] profileImageBytes = decodeBase64Image(dto.getProfileImageBase64());
                userForImmediateUpdate.setProfileImage(profileImageBytes);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("image", "Formato de imagen inválido"));
            }
        }

        Map<String, Object> responseBody = new HashMap<>();

        try {
            service.updateProfile(id, userForImmediateUpdate);
            responseBody.put("message", "Perfil actualizado correctamente.");

            if (dto.getEmail() != null && !dto.getEmail().isBlank()
                    && !dto.getEmail().equalsIgnoreCase(userToUpdate.getEmail())) {
                try {
                    service.requestEmailChange(userToUpdate.getUsername(), dto.getEmail());
                    responseBody.put("emailChangeMessage",
                            "Se ha enviado un correo a " + dto.getEmail() + " para confirmar la nueva dirección.");
                } catch (IllegalArgumentException e) {
                    responseBody.put("emailChangeError", e.getMessage());
                } catch (MessagingException | IOException e) {
                    responseBody.put("emailChangeError",
                            "Error al enviar el correo de confirmación para el nuevo email.");
                }
            }
            return ResponseEntity.ok(responseBody);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/confirm-email-change")
    public RedirectView confirmEmailChange(@RequestParam("token") String token) {
        String redirectUrl;
        try {
            boolean success = service.confirmEmailChange(token);
            if (success) {
                redirectUrl = frontendBaseUrl + "/profile?emailChange=success";
            } else {
                redirectUrl = frontendBaseUrl + "/profile?emailChange=error&reason=invalid_token";
            }
        } catch (IllegalArgumentException e) {
            redirectUrl = frontendBaseUrl + "/profile?emailChange=error&reason=validation_error&message="
                    + e.getMessage();
        } catch (Exception e) {
            redirectUrl = frontendBaseUrl + "/profile?emailChange=error&reason=server_error";
        }
        return new RedirectView(redirectUrl);
    }

    @GetMapping("/emails")
    public ResponseEntity<List<UserReferenceDTO>> getAllEmails() {
        return ResponseEntity.ok(service.findAllEmails());
    }

    private byte[] decodeBase64Image(String base64String) {
        if (base64String.startsWith("data:image")) {
            base64String = base64String.substring(base64String.indexOf(",") + 1);
        }
        return Base64.getDecoder().decode(base64String);
    }
}
