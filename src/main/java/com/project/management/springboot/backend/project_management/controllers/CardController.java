package com.project.management.springboot.backend.project_management.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.management.springboot.backend.project_management.DTO.CardDTO;
import com.project.management.springboot.backend.project_management.entities.models.User;
import com.project.management.springboot.backend.project_management.repositories.UserRepository;
import com.project.management.springboot.backend.project_management.services.card.CardService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "${app.front-url}", originPatterns = "*")
@RequestMapping("/cards")
public class CardController {

    @Autowired
    private CardService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER')")
    public List<CardDTO> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> view(@PathVariable Long id) {
        Optional<CardDTO> cardOptional = service.findById(id);
        if (cardOptional.isPresent()) {
            return ResponseEntity.ok(cardOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> create(@Valid @RequestBody CardDTO cardDTO, BindingResult result) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.save(cardDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> update(@Valid @RequestBody CardDTO cardDTO, BindingResult result,
            @PathVariable Long id) {
        if (result.hasFieldErrors()) {
            return validation(result);
        }
        Optional<CardDTO> cardOptional = service.update(id, cardDTO);
        if (cardOptional.isPresent()) {
            return ResponseEntity.ok(cardOptional.orElseThrow());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-cards")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<CardDTO>> getCardsByCurrentUser() {
        try {
            List<CardDTO> cards = service.findCardsByCurrentUser();
            return ResponseEntity.ok(cards);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/boards/{boardId}/cards")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<CardDTO>> getCardsForBoard(@PathVariable Long boardId) {
        Long currentUserId = getCurrentUserId();
        List<CardDTO> cards = service.getCardsForBoard(boardId, currentUserId);
        return ResponseEntity.ok(cards);
    }

    @PreAuthorize("hasRole('USER')")
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        String username = authentication.getName();

        Optional<User> currentUser = userRepository.findByUsername(username);
        if (currentUser.isEmpty()) {
            throw new AccessDeniedException("Usuario no encontrado");
        }

        return currentUser.get().getId();
    }

    @GetMapping("/boards/{boardId}/cards/status-id/{statusId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<CardDTO>> getCardsByBoardAndStatus(
            @PathVariable Long boardId,
            @PathVariable Long statusId) {

        Long currentUserId = getCurrentUserId();
        List<CardDTO> cards = service.getCardsByBoardAndStatus(boardId, statusId, currentUserId);
        return ResponseEntity.ok(cards);
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
